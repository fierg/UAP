package uap.generator

import uap.Instruction
import uap.node.Node
import uap.node.address.AddressPair
import uap.node.address.TramLabel

class TramCodeGenerator(private val ast: Node) {

    private val instructions: List<Instruction> = mutableListOf()
    private val labels: List<TramLabel> = mutableListOf()

    fun generate(): List<Instruction> {
        val rho = ast.elab_def(mutableMapOf<String, AddressPair>(), 0)
        return ast.code(rho, 0)
    }
}
