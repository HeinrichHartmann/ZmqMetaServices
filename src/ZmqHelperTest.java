import org.jeromq.ZMQ;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by hartmann on 2/15/14.
 */
public class ZmqHelperTest {

    ZMQ.Context ctx;
    ZMQ.Socket req;
    ZMQ.Socket rep;

    String address = "inproc://test";

    @Before
    public void setUp() throws Exception {
        ctx = ZMQ.context();
        req = ctx.socket(ZMQ.REQ);
        rep = ctx.socket(ZMQ.REP);

        req.setSendBufferSize(10);

        rep.bind(address);
        req.connect(address);
    }

    @After
    public void tearDown() throws Exception {
        req.close();
        rep.close();
        ctx.term();
    }

    @Test
    public void testSndRecvAll() throws Exception {
        String frames []  = new String[] {"A", "B" , "C"};
        ZmqHelper.sndAll(req, frames);
        String answer [] = ZmqHelper.recvAll(rep);

        assertArrayEquals(frames, answer);
    }

    @Test
    public void testEmptySndRecvAll() throws Exception {
        String frames []  = new String[] {};
        ZmqHelper.sndAll(req, frames);
        String answer [] = ZmqHelper.recvAllNoBlock(rep);

        assertArrayEquals(frames, answer);
    }
}
