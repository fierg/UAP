
package uap.node;

import uap.Instruction;
import uap.node.address.AddressPair;

import java.util.List;
import java.util.Map;

public class AssignNode extends Node
{
    public AssignNode()
    {
        super("ASSIGN");
    }

    @Override
    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        System.out.println("Handling assign node on level " + nl);

        getChildren().forEach(node -> {
            if (IDNode.class.equals(node.getClass())){
                rho.put((String) node.getAttribute(), addressFactory.getNewIntegerAddressPair(nl));
            } else
                node.elab_def(rho,nl);
        });
        return rho;
    }

    @Override
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        ExprNode expr = (ExprNode) this.getChildren().get(0);
        IDNode id = (IDNode) this.getChildren().get(1);

        List<Instruction> instructions = expr.code(rho, nl);
        AddressPair addressPair = rho.get(id);
        int nlDash = addressPair.getNl();
        int nestingLevelToUse = nl - nlDash;
        instructions.add(new Instruction(Instruction.STORE, (Integer) addressPair.getLoc(), nestingLevelToUse));
        return instructions;
    }
}
