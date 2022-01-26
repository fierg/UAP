package uap.generator

import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleDirectedGraph
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.node.*

class ControlFlowGraphGenerator(private val ast: Node) {

    private val functionEnvironment = mutableMapOf<String,Pair<CFGNode,CFGNode>>()

    fun generate(): CFG {
        return generateCFG(ast)
    }

    private fun generateCFG(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge> = SimpleDirectedGraph<CFGNode, Edge>(Edge::class.java)
    ): CFG {

        when (node) {
            is IDNode -> return handleConstOrIDNode(node, graph)
            is ConstNode -> return handleConstOrIDNode(node, graph)
            is AssignNode -> return handleAssignNode(node, graph)
            is OpNode -> return handleOptNode(node, graph)
            is ReadNode -> return handleDefaultSingleChild(node.children.first, graph)
            is SemiNode -> return handleSemiNode(node, graph)
            is IfNode -> return handleIfNode(node,graph)
            is WhileNode -> return handleWhileNode(node,graph)
            is LetNode -> return handleLetNode(node,graph)
            is DefNode -> return handleDefNode(node, graph)
            is BodyNode -> return handleDefaultSingleChild(node.children.first, graph)
            is CallNode -> return handleCallNode(node,graph)
            else -> {
                println("Unhandled node of type ${node.type}")
                node.children.forEach {
                    generateCFG(it)
                }
                // return CFG(graph,lastIn,lastOut)
            }
        }

        // return graph, in, out
        return CFG(graph, null, null)
    }

    private fun handleDefNode(
        node: DefNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        TODO("Not yet implemented")
    }

    private fun handleCallNode(
        node: CallNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        TODO("Not yet implemented")
    }

    private fun handleDefaultSingleChild(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{

        if (node.children.size > 1)
            TODO("Not yet implemented")
        else
            return generateCFG(node.children.first, graph)
    }


    //TODO handle start and end nodes
    private fun handleLetNode(
        node: LetNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val bodyNode = node.children.filterIsInstance<BodyNode>().first()
        val functionResults = mutableListOf<Triple<SimpleDirectedGraph<CFGNode, Edge>, CFGNode?, CFGNode?>>()
        node.children.filterIsInstance<DefNode>()[0].children.forEach { defNode ->
            val funcNode = defNode.children.first
            val funcResult = generateCFG(funcNode)
            functionResults.add(funcResult)
            functionEnvironment[defNode.children.filterIsInstance<IDNode>().first().attribute as String] = Pair(funcResult.cfgIn!!, funcResult.cfgOut!!)
        }
        val bodyResult = generateCFG(bodyNode)

        return CFG(graph,bodyResult.cfgIn, bodyResult.cfgOut)
    }

    private fun handleWhileNode(
        node: WhileNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val condition = node.children.filterIsInstance<CondNode>().first()
        val expression = node.children.filterIsInstance<ExprNode>().first()

        val condResult = generateCFG(condition,graph)
        val expResult = generateCFG(expression,graph)

        val diamond = CFGNode(node,"diamond")
        val glue = CFGNode(node,"glue")

        Graphs.addGraph(graph,condResult.first)
        Graphs.addGraph(graph,expResult.first)

        //This edge seems wrong... but is listed in the slides
        graph.addEdge(condResult.cfgOut,expResult.cfgIn)

        graph.addEdge(condResult.cfgOut,diamond)
        graph.addEdge(diamond,expResult.cfgIn, Edge("T"))
        graph.addEdge(diamond,glue, Edge("F"))
        graph.addEdge(expResult.cfgOut,condResult.cfgIn)

        return CFG(graph,condResult.cfgIn,glue)
    }

    private fun handleIfNode(
        node: IfNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val condition = generateCFG(node.children.filterIsInstance<CondNode>().first())
        val thenNode = generateCFG(node.children.filterIsInstance<ThenNode>().first())
        val elseNode = generateCFG(node.children.filterIsInstance<ElseNode>().first())
        val diamond = CFGNode(node,"diamond")
        val glue = CFGNode(node,"glue")

        Graphs.addGraph(graph,condition.first)
        Graphs.addGraph(graph,thenNode.first)
        Graphs.addGraph(graph,elseNode.first)

        graph.addVertex(diamond)
        graph.addVertex(glue)

        graph.addEdge(condition.cfgOut,diamond)
        graph.addEdge(diamond,thenNode.cfgIn)
        TODO()
    }

    private fun handleAssignNode(
        node: AssignNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val result1 = generateCFG(node.children[0])
        Graphs.addGraph(graph, result1.first)
        val cfgNode = CFGNode(node = node)
        graph.addVertex(cfgNode)
        graph.addEdge(result1.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)

    }

    private fun handleSemiNode(
        node: SemiNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val result1 = generateCFG(node.children[0])
        val result2 = generateCFG(node.children[1])
        Graphs.addGraph(graph, result1.first)
        Graphs.addGraph(graph, result2.first)
        graph.addEdge(result1.cfgOut, result2.cfgIn)

        return CFG(graph, result1.cfgIn, result2.cfgOut)
    }

    private fun handleOptNode(
        node: OpNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG{
        val result1 = generateCFG(node.children[0])
        val result2 = generateCFG(node.children[1])

        Graphs.addGraph(graph, result1.first)
        Graphs.addGraph(graph, result2.first)

        val cfgNode = CFGNode(node = node)

        graph.addEdge(result1.cfgOut, result2.cfgIn)
        graph.addEdge(result2.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)
    }


    private fun handleConstOrIDNode(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val cfgNode = CFGNode(node)
        graph.addVertex(cfgNode)
        return CFG(graph, cfgNode, cfgNode)
    }

}
