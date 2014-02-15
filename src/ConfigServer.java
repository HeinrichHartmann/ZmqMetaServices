import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jeromq.ZMQ;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hartmann on 2/15/14.
 */
public class ConfigServer {

    private static final String REGEXP = "^(GET) (\\S*)$";
    // OPTIONS
    public static File configFile = new File("config.yaml");
    public static String zmqAddress = "tcp://*:6000";


    // LOGGING
    private static Logger Log = Logger.getLogger(ConfigServer.class);
    static{
        BasicConfigurator.configure();
    }


    public static void main(String[] args) {
        try {
            Map<String, Object> map = loadConfig(configFile);
            serveConfig(map);

        } catch (IOException e) {
            Log.error(e);
        }
    }

    private static void serveConfig(Map<String, Object> config) {
        ZMQ.Context ctx = ZMQ.context();
        ZMQ.Socket  sock = ctx.socket(ZMQ.REP);
        sock.bind(zmqAddress);
        Log.info("Listening on " + zmqAddress);

        while(true) {
            try {
                String[] frames = ZmqHelper.recvAll(sock);

                checkFrames(frames);

                Pattern protocol = Pattern.compile(REGEXP);
                Matcher matcher = protocol.matcher(frames[1]);
                String cmd   = matcher.group(0);
                String key = matcher.group(1);

                if (cmd.equals("GET")) {
                    String value = (String) config.get(key);
                    ZmqHelper.sndAll(sock, new String[]{"ZCS01", "OK", value});
                } else {
                    throw new ProtocolException("CMD NOT SUPPORTED: " + cmd);
                }

            } catch (ProtocolException e) {
                ZmqHelper.sndAll(sock, new String[]{"ZCS01", "NOK", e.toString()});
            }
        }
    }


    
    
    /**
     * Checks that the array of frames conforms to the ZCS Protocol
     *
     * @param frames
     * @throws ProtocolException
     */
    private static void checkFrames(String[] frames) throws ProtocolException {
        if (!(frames.length >= 2 && frames.length <= 3) ){
            throw new ProtocolException("Wrong number of frames");
        }

        if (! frames[0].equals("ZCS01")) {
            throw new ProtocolException("Wrong version string");
        }

        if (! frames[1].matches(REGEXP) ) {
            throw new ProtocolException("Command not supported.");
        }
    }


    public static Map<String, Object> loadConfig(File file) throws IOException {
        Map<String, Object> out = new HashMap<String, Object>();

        Yaml yaml = new Yaml();

        Log.info("Reading config file " + file.getAbsolutePath() );

        try (InputStream input = new FileInputStream(file)) {
            Log.info("Deserializing Object");
            Map map = (Map) yaml.load(input);

            for (Object key: map.keySet()) {
                out.put((String) key, map.get(key));
            }
        }

        Log.info("Parsed Config:\n" + out);
        return out;
    }

    public static void storeConfig(Map<String, Object> map, File file) throws IOException {

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        try (Writer output = new FileWriter(file)){
            yaml.dump(map, output);
        }
    }

    static class ProtocolException extends Exception {
        ProtocolException() {
        }

        ProtocolException(String message) {
            super(message);
        }

        ProtocolException(String message, Throwable cause) {
            super(message, cause);
        }

        ProtocolException(Throwable cause) {
            super(cause);
        }

        ProtocolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
