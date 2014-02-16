#!/usr/bin/env python
#
# ZMQ Config Server
#
# Read config from file and make it accessible as service
#

import zmq
import yaml
import logging as log
import ZCS

FILENAME = "conf.yaml"
ENDPOINT = "tcp://*:7001"

log.basicConfig(level=log.INFO)

def main():
    confserver = ConfServer(FILENAME, ENDPOINT)
    confserver.serve()

class ConfServer:
    config_file = None
    config = {}
    zholder = None
    cholder = None
    run_flag = None

    def __init__(self, config_file, endpoint):
        self.zholder = ZCS.ZmqServerHandler(endpoint)
        self.cholder = ConfHolder(config_file)

    def serve(self):
        log.info("Starting ConfServer")
        self.run_flag = True
        while (self.run_flag):
            try:
                cmd, args = self.zholder.recvCmd()
                log.info("Received Command " + str(cmd) + " - " + str(args))
                response  = self.dispatch(cmd,args)
                self.zholder.sendOk(response)

            except KeyboardInterrupt:
                self.run_flag = False

            except Exception as e:
                self.zholder.sendErr([ str(type(e)) , e.message ])


        log.info("Shutting down ConfServer")
        self.zholder.close()
                            
    def dispatch(self, cmd, args):
        log.info("Received cmd " + cmd)
        if cmd == ZCS.PROTOCOL.GET:
            log.info("CALLED GET")
            return [self.cholder.get(args[0])]

        elif cmd == ZCS.PROTOCOL.SET:
            log.info("CALLED SET")
            self.cholder.put(args[0], args[1])
            return []

        elif cmd == ZCS.PROTOCOL.QUIT:
            log.info("CALLED QUIT")
            self.run_flag = False
            return []

        else:
            raise ValueError("COMMAND NOT FOUND " + cmd)

class ConfHolder:
    filename = None
    config   = None

    def __init__(self, filename):
        self.filename = filename
        self.readconfig()        

    def readconfig(self):
        with open(self.filename) as fh:
            self.config = yaml.load(fh)

        assert (type(self.config) is dict)

    def writeconfig(self):
        with open(self.filename, 'w') as fh:
            yaml.dump(self.config, fh, default_flow_style=False)

    def get(self, key):
        return self.config[key]
        
    def put(self, key, value):
        self.config[key] = value
        self.writeconfig()
        

if __name__ == "__main__":
    main()
