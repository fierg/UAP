
package uap.node;

import uap.Instruction;
import uap.generator.adress.AddressPair;
import uap.node.interfaces.ITramcodeGeneratable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConstNode extends Node implements ITramcodeGeneratable {
    public ConstNode(Integer value) {
        super("CONST", value);
    }

    @Override
    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        return null;
    }

    @Override
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        LinkedList<Instruction> list = new LinkedList<Instruction>();
        list.add(new Instruction(Instruction.CONST, (Integer) this.getAttribute()));
        return list;
    }
}
