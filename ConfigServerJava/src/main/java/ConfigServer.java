import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jeromq.ZMQ;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hartmann on 2/15/14.
 */
public class ConfigServer {

    private static final String REGEXP = "^(GET) (\\S*)$";
    // OPTIONS
    public static String configFile = "config.yaml";
    public static String zmqAddress = "tcp://*:6000";


    // LOGGING
    private static Logger Log = Logger.getLogger(ConfigServer.class);
    static{
        BasicConfigurator.configure();
    }

    private static void parseArgs(String [] args) {

        Options options = new Options();
        Option logfile = OptionBuilder
                .withArgName("file")
                .hasArg()
                .withDescription(  "read config from this file" )
                .create( "config" );

        Option endpoint = OptionBuilder
                .withArgName("address")
                .hasArg()
                .withDescription("serve config on this endpoint")
                .create("endpoint");

        options.addOption(endpoint);
        options.addOption(logfile);

        CommandLineParser parser = new GnuParser();

        try {
            CommandLine line = parser.parse(options, args);
            configFile = line.getOptionValue("config");
            zmqAddress = line.getOptionValue("endpoint");

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);
            Map<String, Object> map = loadConfig(new File(configFile));
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

                String cmd = frames[1];
                String key = frames[2];

                if (cmd.equals("GET")) {
                    Object value = (String) config.get(key);

                    if (value == null) {
                        throw new IllegalArgumentException("Key not found");
                    }

                    ZmqHelper.sndAll(sock, new String[]{"ZCS01", "OK", (String) value});

                } else {
                    throw new ProtocolException("CMD NOT SUPPORTED: " + cmd);
                }

            } catch (Exception e) {
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
        if (!(frames.length == 3) ){
            throw new ProtocolException("Wrong number of frames");
        }

        if (! frames[0].equals("ZCS01")) {
            throw new ProtocolException("Wrong version string");
        }

        if (! frames[1].matches("(GET)") ) {
            throw new ProtocolException("Command not supported.");
        }

        if (! frames[2].matches("^\\S*$") ) {
            throw new ProtocolException("Illegal Value.");
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
