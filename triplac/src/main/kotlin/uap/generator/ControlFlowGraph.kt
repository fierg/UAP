package uap.generator

import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import uap.cfg.CFGNode
import uap.node.AssignNode
import uap.node.ConstNode
import uap.node.IDNode
import uap.node.Node

class ControlFlowGraph(val ast: Node) {
    val graph = SimpleGraph<CFGNode, DefaultEdge>(DefaultEdge::class.java)
    val map = mutableMapOf<Node,CFGNode>()
    var lastIn: Node? = null
    var lastOut: Node? = null


    fun generate() {
        generateCFG(ast)
    }

    private fun generateCFG(node: Node) {

        when (node) {
            is IDNode -> {
                val cfgNode = CFGNode(node, node, node)
                map[node] = cfgNode
                graph.addVertex(cfgNode)
                lastIn = node
                lastOut = node
            }
            is ConstNode -> {
                val cfgNode = CFGNode(node, node, node)
                map[node] = cfgNode
                graph.addVertex(cfgNode)
                lastIn = node
                lastOut = node
            }
            is AssignNode -> {
                generateCFG(node.children[1])
                val cfgNode = CFGNode(lastIn,node,node)
                map[node] = cfgNode
                graph.addVertex(cfgNode)
                graph.addEdge(map[lastOut],cfgNode)
                lastOut = node

            }
            else -> node.children.forEach { generateCFG(it) }
        }
    }

}
