package kianxali.scripting;

import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.gui.Controller;

import org.jruby.Ruby;
import org.jruby.embed.ScriptingContainer;

public class ScriptManager {
    private static final Logger LOG = Logger.getLogger("kianxali.scripting");
    private final Controller controller;
    private final ScriptingContainer ruby;

    public ScriptManager(Controller controller) {
        this.controller = controller;

        this.ruby = new ScriptingContainer();
        Ruby.setThreadLocalRuntime(ruby.getProvider().getRuntime());
        ruby.setWriter(controller.getLogWindowWriter());

        LOG.config("Using Ruby version: " + ruby.getCompatVersion());
    }

    public void runScript(String script) {
        try {
            ruby.runScriptlet(script);
        } catch(Exception e) {
            String msg = "Couldn't run script: " + e.getMessage();
            LOG.log(Level.WARNING, msg, e);
            controller.showError(msg);
        }
    }
}
