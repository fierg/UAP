package uap.generator

import uap.Instruction
import uap.node.ConstNode
import uap.node.DefNode
import uap.node.FuncNode
import uap.node.Node
import uap.node.address.AddressFactory
import uap.node.address.AddressPair
import uap.node.address.LabelAddressPair
import uap.node.address.TramLabel

class TramCodeGenerator(private val ast: Node) {

    private val instructions = mutableListOf<Pair<Instruction, AddressPair?>>()
    private val addressFactory = AddressFactory()

    fun generate(): MutableList<Pair<Instruction, AddressPair?>> {
        val rho = ast.elab_def(mutableMapOf<String, AddressPair>(), 0)
        println("Rho: { $rho }")

        codeNode(ast, rho)

        return instructions
    }

    private fun codeNode(node: Node, rho: Map<String, AddressPair>) {
        when (node) {
            is FuncNode -> {
                handleFuncNode(node, rho)
            }
            is DefNode -> {
                println("handling def node")
                node.children.forEach { codeNode(it, rho) }
            }
            is ConstNode -> {}

            else -> node.children.forEach { codeNode(it, rho) }

        }
    }

    private fun handleFuncNode(node: FuncNode, rho: Map<String, AddressPair>) {
        println("Coding func node...")
        val ap = getFuncNameFromRho(node, rho)
        val label = addressFactory.getNewLabelAddressPair(
            -1,
            ap?.nl ?: throw IllegalStateException("Nesting level for function name not found!")
        )
        val goto = Pair(Instruction(Instruction.GOTO), label)
        instructions.add(goto)
        val pos = instructions.size - 1
        //handle function body
        node.children[2].children.forEach { codeNode(it, rho) }

        instructions.add(Pair(Instruction(Instruction.RETURN), null))
        correctGoToLabel(ap, pos)
    }

    private fun correctGoToLabel(
        ap: AddressPair,
        pos: Int
    ) {
        val label = addressFactory.getNewLabelAddressPair(instructions.size, ap.nl)
        instructions.removeAt(pos)
        instructions.add(pos, Pair(Instruction(Instruction.GOTO,instructions.size + 1), label))
    }

    private fun getFuncNameFromRho(node: FuncNode, rho: Map<String, AddressPair>): AddressPair? {
        return rho[node.children[0].attribute]
    }


}
