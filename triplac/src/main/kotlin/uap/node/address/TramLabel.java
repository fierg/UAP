package uap.node.address;

import uap.Instruction;

public class TramLabel {
    public final Instruction from;
    public Instruction to;
    public TramLabel(Instruction instruction) {
        from = instruction;
    }
    public void setToLabel(Instruction instruction){
        to = instruction;
    }
}
