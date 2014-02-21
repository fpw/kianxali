package kianxali.scripting;

import java.util.logging.Level;
import java.util.logging.Logger;

import kianxali.decoder.DecodedEntity;
import kianxali.decoder.Instruction;
import kianxali.disassembler.Disassembler;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.InstructionVisitor;
import kianxali.gui.Controller;
import kianxali.image.ByteSequence;
import kianxali.image.ImageFile;
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
    public DecodedEntity getEntityAt(Long addr) {
        DisassemblyData data = controller.getDisassemblyData();
        if(data == null || addr == null) {
            return null;
        }
        return data.getEntityOnExactAddress(addr);
    }

    @Override
    public boolean isCodeAddress(Long addr) {
        ImageFile image = controller.getImageFile();
        if(image == null || addr == null) {
            return false;
        }
        return image.isCodeAddress(addr);
    }

    @Override
    public void patchByte(Long addr, Short b) {
        if(addr == null || b == null) {
            throw new IllegalArgumentException("null-address or byte");
        }

        ImageFile image = controller.getImageFile();
        if(image == null) {
            throw new IllegalStateException("no image loaded");
        }

        ByteSequence seq = image.getByteSequence(addr, true);
        seq.patch((byte) (b & 0xFF));
        seq.unlock();
    }

    @Override
    public void reanalyze(Long addr) {
        if(addr == null) {
            throw new IllegalArgumentException("null-address passed");
        }

        Disassembler dasm = controller.getDisassembler();
        if(dasm == null) {
            throw new IllegalStateException("no disassembler loaded");
        }

        dasm.reanalyze(addr);
    }

    @Override
    public Short readByte(Long addr) {
        if(addr == null) {
            throw new IllegalArgumentException("null-address");
        }

        ImageFile image = controller.getImageFile();
        if(image == null) {
            throw new IllegalStateException("no image loaded");
        }

        ByteSequence seq = image.getByteSequence(addr, true);
        Short res = seq.readUByte();
        seq.unlock();
        return res;
    }
}