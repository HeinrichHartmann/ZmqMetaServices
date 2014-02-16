
import ZCS
import unittest
import ConfServer
import os

import time

from threading import Thread
from multiprocessing import Process

FILENAME = "TEST_CASE.yaml"
ENDPOINT = "tcp://127.0.0.1:7001"

class TestConfServer(unittest.TestCase):

    cs = None
    zc = None
    process = None

    def setUp(self):
        fh = open(FILENAME,'w')
        fh.write("HELLO: WORLD\n")
        fh.close()

        # start server in background
        def serve():
            cs = ConfServer.ConfServer(FILENAME, ENDPOINT)
            cs.serve()

        self.process = Process(target=serve)
        self.process.start()

        self.zc = ZCS.ZmqClientHandler(ENDPOINT)

    def tearDown(self):
        self.zc.doQuit()
        self.process.join(0.1)
        self.process.terminate()
        os.remove(FILENAME)

    def testHelloWorld(self):
        self.assertListEqual( 
            self.zc.doGet("HELLO"),
            [ZCS.PROTOCOL.VERSION, ZCS.PROTOCOL.OK, "WORLD"]
        )

    def testNoKey(self):
        self.assertListEqual( 
            self.zc.doGet("XXX")[0:2],
            [ZCS.PROTOCOL.VERSION, ZCS.PROTOCOL.ERR]
        )

    def testSetKey(self):
        self.assertListEqual(
            self.zc.doSet("MY", "WAY"),
            [ZCS.PROTOCOL.VERSION, ZCS.PROTOCOL.OK]
        )
        self.assertListEqual( 
            self.zc.doGet("MY")[0:3],
            [ZCS.PROTOCOL.VERSION, ZCS.PROTOCOL.OK, "WAY"]
        )

    def testLoadTest(self):
        for i in xrange(100):
            self.zc.doSet("SQ_" + str(i), str(i*i))
            

if __name__ == '__main__':
    unittest.main()



