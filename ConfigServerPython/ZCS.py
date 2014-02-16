import zmq

class PROTOCOL:
    VERSION = "ZCS01"

    # FLAGS
    OK      = "OK"
    ERR     = "NOK"

    # COMMANDS
    GET     = "GET"
    SET     = "SET"
    QUIT    = "QUIT"

    COMMANDS = [
        GET, 
        SET,
        QUIT
    ]

    @classmethod
    def validate(cls, frames):
        return (
            type(frames) is list      and 
            len(frames) >= 2          and
            frames[0] == cls.VERSION  and
            frames[1] in cls.COMMANDS 
        )

class ZmqHandler(object):
    ctx = None
    socket = None
    
    def __init__(self, ctx, socket):
        self.ctx = ctx
        self.socket = socket

    def close(self):
        self.socket.close()
        self.ctx.destroy()

class ZmqServerHandler(ZmqHandler):

    def __init__(self, endpoint, ctx = zmq.Context()):
        self.ctx = ctx
        self.socket = self.ctx.socket(zmq.REP)
        self.socket.bind(endpoint)

    def sendOk(self, msg):
        assert(type(msg) is list)
        self.socket.send_multipart([PROTOCOL.VERSION,PROTOCOL.OK] + msg)

    def sendErr(self, msg):
        assert(type(msg) is list)
        self.socket.send_multipart([PROTOCOL.VERSION, PROTOCOL.ERR] + msg)

    def recvCmd(self):
        """
        Loop until valid message is received.
        Return (
            cmd -- command string, 
            arg -- list of arguments
        )
        """

        while (True):
            frames = self.socket.recv_multipart()

            if (not PROTOCOL.validate(frames)):
                self.sendErr("ILLEGAL REQUEST")
            else:
                break

        # Have valid frames
        cmd = frames[1]
        args = frames[2:]

        return (cmd, args)


class ZmqClientHandler(ZmqHandler):

    def __init__(self, endpoint, ctx = zmq.Context()):
        self.ctx = ctx
        self.socket = self.ctx.socket(zmq.REQ)
        self.socket.connect(endpoint)

    def doQuit(self):
        self.socket.send_multipart([PROTOCOL.VERSION,PROTOCOL.QUIT])
        return self.socket.recv_multipart()

    def doGet(self, key):
        assert(type(key) is str)
        self.socket.send_multipart([PROTOCOL.VERSION,PROTOCOL.GET, key])
        return self.socket.recv_multipart()

    def doSet(self, key, value):
        assert(type(key) is str)
        assert(type(value) is str)
        self.socket.send_multipart([PROTOCOL.VERSION,PROTOCOL.SET, key, value])
        return self.socket.recv_multipart()
