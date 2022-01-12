package uap.node;

import uap.Instruction;
import uap.generator.adress.AddressPair;
import uap.node.interfaces.ITramcodeGeneratable;

import java.util.List;
import java.util.Map;

public class AstNode implements ITramcodeGeneratable {

    @Override
    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        return null;
    }

    @Override
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        return null;
    }
}
