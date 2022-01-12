package uap.generator

import uap.Instruction
import uap.generator.adress.TramLabel
import uap.node.Node

class TramCodeGenerator(ast: Node) {

    private val instructions: List<Instruction> = mutableListOf()
    private val labels: List<TramLabel> = mutableListOf()

    fun generate(): List<Instruction> {
        val instructions = mutableListOf<Instruction>()



        return instructions
    }

    fun elabDef() {

    }

}
