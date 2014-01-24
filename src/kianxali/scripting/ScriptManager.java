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
    private final ScriptAPI api;

    public ScriptManager(Controller controller) {
        this.controller = controller;
        this.ruby = new ScriptingContainer();
        this.api = new ScriptAPIImpl(ruby, controller);
        Ruby.setThreadLocalRuntime(ruby.getProvider().getRuntime());
        ruby.setWriter(controller.getLogWindowWriter());
        ruby.put("$api", api);

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
