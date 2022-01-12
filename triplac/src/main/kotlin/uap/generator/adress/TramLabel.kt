package uap.generator.adress

import uap.Instruction

class TramLabel(val from: Instruction, var to: Instruction?) {

    fun bind(instruction: Instruction) {
        to = instruction
    }
}