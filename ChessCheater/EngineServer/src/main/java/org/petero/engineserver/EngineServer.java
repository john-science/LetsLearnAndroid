package org.petero.engineserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;


/** Manages starting and stopping PortListeners. */
public class EngineServer implements ErrorHandler {
    private EngineConfig[] configs;
    private PortListener[] portListeners;
    private MainWindow window;

    private EngineServer(int numEngines) {
        configs = new EngineConfig[numEngines];
        portListeners = new PortListener[numEngines];
        for (int i = 0; i < numEngines; i++) {
            configs[i] = new EngineConfig(false, 4567 + i, "", "");
        }
        readConfig();
        for (int i = 0; i < numEngines; i++)
            configChanged(i);
    }

    private File getConfigFile() {
        String home = System.getProperty("user.home");
        return new File(home, ".engineServer.ini");
    }

    private void readConfig() {
        try {
            Properties prop = new Properties();
            InputStream is = new FileInputStream(getConfigFile());
            prop.load(is);
            for (int i = 0; i < configs.length; i++) {
                boolean enabled = Boolean.parseBoolean(prop.getProperty("enabled" + i, "false"));
                String defPort = Integer.toString(4567 + i);
                int port = Integer.parseInt(prop.getProperty("port" + i, defPort));
                String filename = prop.getProperty("filename" + i, "");
                String arguments = prop.getProperty("arguments" + i, "");
                configs[i] = new EngineConfig(enabled, port, filename, arguments);
            }
        } catch (IOException | NumberFormatException ignore) {
        }
    }

    private void writeConfig() {
        Properties prop = new Properties();
        for (int i = 0; i < configs.length; i++) {
            EngineConfig config = configs[i];
            String enabled = config.enabled ? "true" : "false";
            String port = Integer.toString(config.port);
            String filename = config.filename;
            String arguments = config.arguments;
            prop.setProperty("enabled" + i, enabled);
            prop.setProperty("port" + i, port);
            prop.setProperty("filename" + i, filename);
            prop.setProperty("arguments" + i, arguments);
        }
        try {
            OutputStream os = new FileOutputStream(getConfigFile());
            prop.store(os, "Created by EngineServer for DroidFish");
        } catch (IOException ignore) {
        }
    }

    private void runGui() {
        window = new MainWindow(this, configs);
    }

    public void configChanged(int engineNo) {
        EngineConfig config = configs[engineNo];
        if (portListeners[engineNo] != null) {
            portListeners[engineNo].shutdown();
            portListeners[engineNo] = null;
        }
        if (config.enabled)
            portListeners[engineNo] = new PortListener(config, this);
    }

    public void shutdown() {
        writeConfig();
        for (PortListener pl : portListeners)
            if (pl != null)
                pl.shutdown();
        System.exit(0);
    }

    private static void usage() {
        System.out.println("Usage: engineServer [-numengines value] [-nogui]");
        System.exit(2);
    }

    public static void main(String[] args) {
        int numEngines = 8;
        boolean gui = true;
        for (int i = 0; i < args.length; i++) {
            if ("-numengines".equals(args[i]) && i+1 < args.length) {
                try {
                    numEngines = Integer.parseInt(args[i+1]);
                    numEngines = Math.max(1, numEngines);
                    numEngines = Math.min(20, numEngines);
                    i++;
                } catch (NumberFormatException e) {
                    usage();
                }
            } else if ("-nogui".equals(args[i])) {
                gui = false;
            } else {
                usage();
            }
        }
        EngineServer server = new EngineServer(numEngines);
        if (gui)
            server.runGui();
    }

    @Override
    public void reportError(String title, String message) {
        if (window != null) {
            window.reportError(title, message);
        } else {
            System.err.printf("%s\n%s\n", title, message);
        }
    }
}
