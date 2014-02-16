import org.jeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hartmann on 2/15/14.
 */
public class ZmqHelper {

    public static void sndAll(ZMQ.Socket sock, String[] frames) {
        if (frames.length == 0 ) { return; }

        for (int i = 0; i < frames.length - 1; i++) {
            sock.send(frames[i], ZMQ.SNDMORE);
        }

        sock.send(frames[frames.length - 1]);
    }

    public static String[] recvAll(ZMQ.Socket socket) {
        return recvAll(socket, false);
    }

    public static String[] recvAllNoBlock(ZMQ.Socket socket) {
        return recvAll(socket, true);
    }

    private static String[] recvAll(ZMQ.Socket socket, boolean NOBLOCK) {
        List<String> frames = new ArrayList<String>(2);

        String msg = null;
        if (NOBLOCK) {
            msg = socket.recvStr(ZMQ.DONTWAIT);
        } else {
            msg = socket.recvStr();
        }

        if (msg == null) { return new String[0]; }

        frames.add(msg);
        while (socket.hasReceiveMore()) {
            frames.add(socket.recvStr());
        }

        return (String[]) frames.toArray(new String[frames.size()]);
    }

}
