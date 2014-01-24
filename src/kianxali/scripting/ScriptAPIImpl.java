package kianxali.scripting;

import org.jruby.RubyProc;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import kianxali.decoder.Instruction;
import kianxali.disassembler.DisassemblyData;
import kianxali.disassembler.InstructionVisitor;
import kianxali.gui.Controller;

public class ScriptAPIImpl implements ScriptAPI {
    private final Controller controller;
    private final ThreadContext rubyContext;

    public ScriptAPIImpl(ScriptingContainer ruby, Controller controller) {
        this.rubyContext = ruby.getProvider().getRuntime().getCurrentContext();
        this.controller = controller;
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
}
