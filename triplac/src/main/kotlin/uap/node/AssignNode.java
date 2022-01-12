
package uap.node;

import uap.Instruction;
import uap.generator.adress.AddressPair;
import uap.node.interfaces.ITramcodeGeneratable;

import java.util.List;
import java.util.Map;

public class AssignNode extends Node implements ITramcodeGeneratable
{
    public AssignNode()
    {
        super("ASSIGN");
    }

    @Override
    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        return null;
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
