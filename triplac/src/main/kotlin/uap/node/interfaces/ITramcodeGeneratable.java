package uap.node.interfaces;

import uap.Instruction;
import uap.generator.adress.AddressPair;

import java.util.List;
import java.util.Map;

public interface ITramcodeGeneratable {
    Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl);
    List<Instruction> code(Map<String, AddressPair> rho, int nl);
}

