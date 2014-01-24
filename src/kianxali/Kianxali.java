package kianxali;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.gui.Controller;
import kianxali.util.LogFormatter;

public final class Kianxali {
    private static final Logger LOG = Logger.getLogger("kianxali");
    private final Controller controller;

    public Kianxali(Level logLevel) {
        // setup logging
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(logLevel);
        consoleHandler.setFormatter(new LogFormatter());
        LOG.setUseParentHandlers(false);
        LOG.addHandler(consoleHandler);
        LOG.setLevel(logLevel);

        LOG.info("Kianxali starting");

        controller = new Controller();
    }

    public void start() {
        controller.showGUI();
    }

    public static void main(String[] args) {
        Kianxali kianxali = new Kianxali(Level.FINEST);
        kianxali.start();
    }
}
