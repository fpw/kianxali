package kianxali.scripting;

import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.InstructionVisitor;
import kianxali.gui.Controller;

import org.jruby.Ruby;
import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class ScriptManager implements ScriptAPI {
    private static final Logger LOG = Logger.getLogger("kianxali.scripting");
    private final Controller controller;
    private final ScriptingContainer ruby;
    private final ThreadContext rubyContext;

    public ScriptManager(Controller controller) {
        this.controller = controller;
        this.ruby = new ScriptingContainer();
        this.rubyContext = ruby.getProvider().getRuntime().getCurrentContext();
        Ruby.setThreadLocalRuntime(ruby.getProvider().getRuntime());
        ruby.setWriter(controller.getLogWindowWriter());
        ruby.put("$api", this);

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

    public IRubyObject toRubyObject(Object object) {
        return JavaEmbedUtils.javaToRuby(rubyContext.getRuntime(), object);
    }

    @Override
    public void traverseCode(final RubyProc block) {
        DisassemblyData data = controller.getDisassemblyData();
        if(data == null) {
            return;
        }
        data.visitInstructions(new InstructionVisitor() {
            @Override
            public void onVisit(Instruction inst) {
                IRubyObject[] args = {toRubyObject(inst)};
                block.call(rubyContext, args);
            }
        });
    }

    @Override
    public DecodedEntity getEntityAt(long addr) {
        DisassemblyData data = controller.getDisassemblyData();
        if(data == null) {
            return null;
        }
        return data.getEntityOnExactAddress(addr);
    }
}
