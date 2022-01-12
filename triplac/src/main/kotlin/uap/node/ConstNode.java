
package uap.node;

import uap.Instruction;
import uap.node.address.AddressPair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConstNode extends Node {
    public ConstNode(Integer value) {
        super("CONST", value);
    }

    @Override
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        LinkedList<Instruction> list = new LinkedList<Instruction>();
        list.add(new Instruction(Instruction.CONST, (Integer) this.getAttribute()));
        return list;
    }
}
