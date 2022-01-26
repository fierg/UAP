package uap.generator

import com.mxgraph.layout.mxCircleLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import org.jgrapht.Graphs
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.SimpleDirectedGraph
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.node.*
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

class ControlFlowGraphGenerator(private val ast: Node) {

    private val functionEnvironment = mutableMapOf<String, Pair<CFGNode, CFGNode>>()

    fun generate(): CFG {
        return generateCFG(ast)
    }

    private fun generateCFG(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge> = SimpleDirectedGraph<CFGNode, Edge>(Edge::class.java)
    ): CFG {

        println("handling node: ${node.type}")
        when (node) {
            is IDNode -> return handleConstOrIDNode(node, graph)
            is ConstNode -> return handleConstOrIDNode(node, graph)
            is AssignNode -> return handleAssignNode(node, graph)
            is OpNode -> return handleOptNode(node, graph)
            is ReadNode -> return handleDefaultSingleChild(node, graph)
            is SemiNode -> return handleSemiNode(node, graph)
            is IfNode -> return handleIfNode(node, graph)
            is WhileNode -> return handleWhileNode(node, graph)
            is LetNode -> return handleLetNode(node, graph)
            is DefNode -> return handleDefNode(node, graph)
            is BodyNode -> return handleDefaultSingleChild(node, graph)
            is CallNode -> return handleCallNode(node, graph)
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
    ): CFG {
        node
        //TODO
        return CFG(graph, null, null)

    }

    private fun handleCallNode(
        node: CallNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val id = node.children.filterIsInstance<IDNode>().first()
        val args = node.children.filterIsInstance<ArgsNode>().first()

        val argCFGs = mutableListOf<CFG>()
        args.children.forEach {
            argCFGs.add(generateCFG(it, graph))
        }

        val functionEntrypoints = functionEnvironment[id.attribute]!!
        val call = CFGNode(node, "CALL")
        val ret = CFGNode(node, "RET")

        graph.addVertex(ret)
        graph.addVertex(call)
        graph.addVertex(functionEntrypoints.first)
        graph.addVertex(functionEntrypoints.second)

        argCFGs.forEach {
            Graphs.addGraph(graph, it.graph)
        }

        argCFGs.forEachIndexed { index, cfg ->
            if (index + 1 < argCFGs.size){
                graph.addEdge(argCFGs[index].cfgOut, argCFGs[index+1].cfgIn)
            }
        }
        graph.addEdge(argCFGs.last().cfgOut,call)
        graph.addEdge(call,functionEntrypoints.first)
        graph.addEdge(functionEntrypoints.second,ret)
        graph.addEdge(call,ret)

        return CFG(graph, argCFGs.first().cfgIn, ret)
    }

    private fun handleDefaultSingleChild(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {

        if (node.children.size > 1)
            TODO("Not yet implemented")
        else
            println("skipping node: ${node.type}")
        return generateCFG(node.children.first, graph)
    }


    //TODO handle start and end nodes
    private fun handleLetNode(
        node: LetNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val bodyNode = node.children.filterIsInstance<BodyNode>().first()
        val functionResults = mutableListOf<CFG>()
        node.children.filterIsInstance<DefNode>()[0].children.forEach { defNode ->
            val funcNode = defNode.children.first
            val funcResult = generateCFG(funcNode)
            functionResults.add(funcResult)
            functionEnvironment[defNode.children.filterIsInstance<IDNode>().first().attribute as String] =
                Pair(funcResult.cfgIn!!, funcResult.cfgOut!!)
        }
        val bodyResult = generateCFG(bodyNode)

        return CFG(graph, bodyResult.cfgIn, bodyResult.cfgOut)
    }

    private fun handleWhileNode(
        node: WhileNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val condition = node.children.filterIsInstance<CondNode>().first()
        val expression = node.children.filterIsInstance<ExprNode>().first()

        val condResult = generateCFG(condition, graph)
        val expResult = generateCFG(expression, graph)

        val diamond = CFGNode(node, "diamond")
        val glue = CFGNode(node, "glue")

        Graphs.addGraph(graph, condResult.graph)
        Graphs.addGraph(graph, expResult.graph)

        //This edge seems wrong... but is listed in the slides
        graph.addEdge(condResult.cfgOut, expResult.cfgIn)

        graph.addEdge(condResult.cfgOut, diamond)
        graph.addEdge(diamond, expResult.cfgIn, Edge("T"))
        graph.addEdge(diamond, glue, Edge("F"))
        graph.addEdge(expResult.cfgOut, condResult.cfgIn)

        return CFG(graph, condResult.cfgIn, glue)
    }

    private fun handleIfNode(
        node: IfNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val condition = generateCFG(node.children.filterIsInstance<CondNode>().first())
        val thenNode = generateCFG(node.children.filterIsInstance<ThenNode>().first())
        val elseNode = generateCFG(node.children.filterIsInstance<ElseNode>().first())
        val diamond = CFGNode(node, "diamond")
        val glue = CFGNode(node, "glue")

        Graphs.addGraph(graph, condition.graph)
        Graphs.addGraph(graph, thenNode.graph)
        Graphs.addGraph(graph, elseNode.graph)

        graph.addVertex(diamond)
        graph.addVertex(glue)

        graph.addEdge(condition.cfgOut, diamond)
        graph.addEdge(diamond, thenNode.cfgIn, Edge("T"))
        graph.addEdge(diamond, elseNode.cfgIn, Edge("F"))
        graph.addEdge(thenNode.cfgOut, glue)
        graph.addEdge(elseNode.cfgOut, glue)

        return CFG(graph, elseNode.cfgIn, glue)
    }

    private fun handleAssignNode(
        node: AssignNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val result1 = generateCFG(node.children[0])
        Graphs.addGraph(graph, result1.graph)
        val cfgNode = CFGNode(node = node)
        graph.addVertex(cfgNode)
        graph.addEdge(result1.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)

    }

    private fun handleSemiNode(
        node: SemiNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val result1 = generateCFG(node.children[0])
        val result2 = generateCFG(node.children[1])
        Graphs.addGraph(graph, result1.graph)
        Graphs.addGraph(graph, result2.graph)
        graph.addEdge(result1.cfgOut, result2.cfgIn)

        return CFG(graph, result1.cfgIn, result2.cfgOut)
    }

    private fun handleOptNode(
        node: OpNode,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val result1 = generateCFG(node.children[0])
        val result2 = generateCFG(node.children[1])

        Graphs.addGraph(graph, result1.graph)
        Graphs.addGraph(graph, result2.graph)

        val cfgNode = CFGNode(node = node)

        graph.addEdge(result1.cfgOut, result2.cfgIn)
        graph.addEdge(result2.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)
    }


    private fun handleConstOrIDNode(
        node: Node,
        graph: SimpleDirectedGraph<CFGNode, Edge>
    ): CFG {
        val cfgNode = CFGNode(node, node.attribute.toString())
        graph.addVertex(cfgNode)
        return CFG(graph, cfgNode, cfgNode)
    }

    fun printGraphToImage(graph: CFG) {
        val graphAdapter: JGraphXAdapter<CFGNode, Edge> = JGraphXAdapter(graph.graph)
        val layout: mxIGraphLayout = mxCircleLayout(graphAdapter)
        layout.execute(graphAdapter.getDefaultParent())

        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)
        val imgFile = File("data/graph.png")
        //ImageIO.write(image, "PNG", imgFile)
    }

}
