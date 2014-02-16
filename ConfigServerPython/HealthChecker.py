#
# ZMQ Health Checker
#
# Receives heartbeats on SUB socket.
#
# Logs status to STDOUT in regular intervals
#

import zmq

class HealthChecker:

    zh = None
    interval = None
    services = []
    
    def __init__(self, endpoint, interval):
        self.interval = interval # in msec
        self.zh = ZmqHealthHandler(endpoint)

    def serve():
        while (True):
            zh.recv()

class ZmqHealthHandler:
    ctx = None
    sub = None

    def __init__(self, endpoint):
        self.ctx = zmq.Context()
        self.sub = self.ctx.socket(zmq.SUB)
        self.sub.bind(endpoint)
