#
# Test Methods for Config Server Classes
#

import unittest
import ZCS

class TestProtocol(unittest.TestCase):

    def setUp(self):
        pass

    def test_validate(self):
        self.assertFalse(ZCS.PROTOCOL.validate([]))
        self.assertFalse(ZCS.PROTOCOL.validate("String"))
        self.assertFalse(ZCS.PROTOCOL.validate(123))

        self.assertFalse(ZCS.PROTOCOL.validate(["ZCS01", "XXX", "XXX"]))

        self.assertTrue(ZCS.PROTOCOL.validate(["ZCS01", "GET", "HELP"]))
        self.assertTrue(ZCS.PROTOCOL.validate(["ZCS01", "SET", "KEY" ,"VALUE"]))

if __name__ == '__main__':
    unittest.main()
