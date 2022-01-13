
package uap.node;

public class ConstNode extends Node {
    public ConstNode(Integer value) {
        super("CONST", value);
    }

    /*
    @Override
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        LinkedList<Instruction> list = new LinkedList<Instruction>();
        list.add(new Instruction(Instruction.CONST, (Integer) this.getAttribute()));
        return list;
    }

     */
}
