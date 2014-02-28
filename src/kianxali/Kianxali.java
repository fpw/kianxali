package kianxali;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.gui.Controller;
import kianxali.util.LogFormatter;

/**
 * Kianxali is an interactive, scriptable disassembler supporting multiple architectures
 * and file types.
 * @author fwi
 *
 */
public final class Kianxali {
    private static final Logger LOG = Logger.getLogger("kianxali");
    private final Controller controller;

    /**
     * Create a new instance of the disassembler
     * @param logLevel the log level to display on the console
     */
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

    /**
     * Starts the actual disassembler by showing the GUI
     */
    public void start() {
        controller.showGUI();
    }

    /**
     * Main entry point of the program: Starts the disassembler and shows the GUI
     * @param args unused
     */
    public static void main(String[] args) {
        Kianxali kianxali = new Kianxali(Level.FINEST);
        kianxali.start();
    }
}
