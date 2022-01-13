package uap.generator

import de.unitrier.st.uap.sym
import uap.Instruction
import uap.node.*
import uap.node.address.AddressFactory
import uap.node.address.AddressPair
import uap.node.address.TramLabel

class TramCodeGenerator(private val ast: Node) {

    private val instructions = mutableListOf<Pair<Instruction, AddressPair?>>()
    private val addressFactory = AddressFactory()

    fun generate(): MutableList<Pair<Instruction, AddressPair?>> {
        val rho = ast.elab_def(mutableMapOf<String, AddressPair>(), 0)
        println("Rho: { $rho }")

        codeNode(ast, rho)
        instructions.add(Pair(Instruction(Instruction.HALT), null))

        return instructions
    }

    private fun codeNode(node: Node, rho: Map<String, AddressPair>) {
        when (node) {
            is FuncNode -> handleFuncNode(node, rho)
            is ConstNode -> {
                instructions.add(Pair(Instruction(Instruction.CONST, node.attribute as Int?), null))
            }
            is ReadNode -> handleReadNode(node, rho)
            is OpNode -> handleOptNode(node, rho)
            is IfNode -> handleIfNode(node, rho)
            is CallNode -> handleCallNode(node, rho)
            is WhileNode -> handleWhileNode(node, rho)
            is SemiNode -> handleSemiNode(node, rho)

            else -> node.children.forEach { codeNode(it, rho) }

        }
    }

    private fun handleSemiNode(node: SemiNode, rho: Map<String, AddressPair>) {
        node.children.forEach {
            codeNode(it, rho)
            instructions.add(Pair(Instruction(Instruction.POP), null))
        }
    }

    private fun handleWhileNode(node: WhileNode, rho: Map<String, AddressPair>) {
        TODO("Not yet implemented")
    }

    private fun handleCallNode(node: CallNode, rho: Map<String, AddressPair>) {
        node.children[1].children.forEach { codeNode(it, rho) }
        val fname = (getFuncNameFromRho(node, rho)!!.loc as TramLabel).address
        val args = node.children[1].children.size

        instructions.add(Pair(Instruction(Instruction.INVOKE, args, fname, 0), null))
    }

    private fun handleIfNode(node: IfNode, rho: Map<String, AddressPair>) {
        node.children[0].children.forEach { codeNode(it, rho) }
        instructions.add(Pair(Instruction(Instruction.IFZERO), null))
        node.children[1].children.forEach { codeNode(it, rho) }
        instructions.add(Pair(Instruction(Instruction.GOTO, -1), null))
        //TODO handle labels
        val pos = instructions.size - 1
        node.children[2].children.forEach { codeNode(it, rho) }
        instructions.add(Pair(Instruction(Instruction.NOP), null))
    }

    private fun handleReadNode(
        node: ReadNode,
        rho: Map<String, AddressPair>
    ) {
        val add = rho[node.children.first.attribute]
        instructions.add(Pair(Instruction(Instruction.LOAD, (add!!.loc as TramLabel).address, add.nl), null))
    }

    private fun handleOptNode(node: OpNode, rho: Map<String, AddressPair>) {
        node.children.forEach { codeNode(it, rho) }
        when (attributeToInt(node.attribute as String)) {
            sym.ADD, sym.OR -> handleOptInstruction(Instruction(Instruction.ADD))
            sym.SUB -> handleOptInstruction(Instruction(Instruction.SUB))
            sym.DIV -> handleOptInstruction(Instruction(Instruction.DIV))
            sym.MULT, sym.AND -> handleOptInstruction(Instruction(Instruction.MUL))
            sym.EQ -> handleOptInstruction(Instruction(Instruction.EQ))
            sym.GT -> handleOptInstruction(Instruction(Instruction.GT))
            sym.LT -> handleOptInstruction(Instruction(Instruction.LT))
            sym.NEQ -> handleOptInstruction(Instruction(Instruction.NEQ))
            else -> handleOptInstruction(Instruction(Instruction.NOP))
        }
    }

    private fun handleOptInstruction(instruction: Instruction) {
        instructions.add(Pair(instruction, null))
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
        instructions.add(pos, Pair(Instruction(Instruction.GOTO, instructions.size + 1), label))
    }

    private fun getFuncNameFromRho(node: FuncNode, rho: Map<String, AddressPair>): AddressPair? {
        return rho[node.children[0].attribute]
    }

    private fun getFuncNameFromRho(node: CallNode, rho: Map<String, AddressPair>): AddressPair? {
        return rho[node.children[0].attribute]
    }

    private fun attributeToInt(attribute: String): Int {
        return when (attribute) {
            "*" -> sym.MULT
            "+" -> sym.ADD
            "-" -> sym.SUB
            "/" -> sym.DIV
            else -> throw IllegalStateException("Sym not found")
        }
    }


}
