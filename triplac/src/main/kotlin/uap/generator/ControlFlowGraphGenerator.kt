package uap.generator

import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleDirectedGraph
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.node.*

class ControlFlowGraphGenerator(private val ast: Node) {

    private val functionEnvironment = mutableMapOf<String, Pair<CFGNode?, CFGNode?>>()

    fun generate(): CFG {
        val cfg = generateCFG(ast, SimpleDirectedGraph<CFGNode, Edge>(Edge::class.java))
        val inNode = CFGNode(ast, "IN")
        val outNode = CFGNode(ast, "OUT")

        cfg.graph.addVertex(inNode)
        cfg.graph.addVertex(outNode)
        cfg.graph.addEdge(inNode, cfg.cfgIn)
        cfg.graph.addEdge(cfg.cfgOut, outNode)

        return CFG(cfg.graph, inNode, outNode)
    }

    private fun generateCFG(node: Node, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        return when (node) {
            is IDNode -> handleConstOrIDNode(node, graph)
            is ConstNode -> handleConstOrIDNode(node, graph)
            is AssignNode -> handleAssignNode(node, graph)
            is OpNode -> handleOptNode(node, graph)
            is SemiNode -> handleSemiNode(node, graph)
            is IfNode -> handleIfNode(node, graph)
            is WhileNode -> handleWhileNode(node, graph)
            is LetNode -> handleLetNode(node, graph)
            is CallNode -> handleCallNode(node, graph)
            is FuncNode -> handleFuncNode(node, graph)
            else -> {
                if (node is CondNode || node is ElseNode || node is ParNode || node is ThenNode || node is ReadNode || node is DefNode || node is BodyNode || node is ExprNode) return handleDefaultSingleChild(
                    node,
                    graph
                )
                println("Unhandled node of type ${node.type}. Performing CFG on first child as workaround.")
                return generateCFG(node.children.first, graph)
            }
        }
    }

    private fun handleFuncNode(node: FuncNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val body = node.children.filterIsInstance<BodyNode>().first()
        val id = node.children.filterIsInstance<IDNode>().first()
        val params = node.children.filterIsInstance<ParamsNode>().first().children.map { it.attribute }.toString()

        val start = CFGNode(node, "START ${id.attribute}$params")
        val end = CFGNode(node, "END ${id.attribute}$params")
        functionEnvironment[id.attribute as String] = Pair(start, end)
        val bodyResult = generateCFG(body, graph)

        graph.addVertex(start)
        graph.addVertex(end)
        graph.addEdge(start, bodyResult.cfgIn)
        graph.addEdge(bodyResult.cfgOut, end)

        return CFG(graph, start, end)
    }

    private fun handleCallNode(node: CallNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val id = node.children.filterIsInstance<IDNode>().first()
        val args = node.children.filterIsInstance<ArgsNode>().first()

        val argCFGs = mutableListOf<CFG>()
        args.children.forEach {
            argCFGs.add(generateCFG(it, graph))
        }

        val functionEntrypoint = functionEnvironment[id.attribute]!!
        val call = CFGNode(node, "CALL")
        val ret = CFGNode(node, "RET")

        graph.addVertex(ret)
        graph.addVertex(call)
        graph.addVertex(functionEntrypoint.first)
        graph.addVertex(functionEntrypoint.second)

        argCFGs.forEach {
            Graphs.addGraph(graph, it.graph)
        }

        argCFGs.forEachIndexed { index, _ ->
            if (index + 1 < argCFGs.size)
                graph.addEdge(argCFGs[index].cfgOut, argCFGs[index + 1].cfgIn)
        }
        graph.addEdge(argCFGs.last().cfgOut, call)
        graph.addEdge(call, functionEntrypoint.first)
        graph.addEdge(functionEntrypoint.second, ret)
        graph.addEdge(call, ret)

        return CFG(graph, argCFGs.first().cfgIn, ret)
    }

    private fun handleDefaultSingleChild(node: Node, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        if (node.children.size > 1)
            throw IllegalArgumentException("Only nodes with a single child supported! Node: ${node.type}")
        else
            return generateCFG(node.children.first, graph)
    }

    private fun handleLetNode(node: LetNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val bodyNode = node.children.filterIsInstance<BodyNode>().first()
        val defResults = mutableListOf<CFG>()

        node.children.filterIsInstance<DefNode>().forEach { defNode ->
            val defResult = generateCFG(defNode, graph)
            defResults.add(defResult)
            functionEnvironment[defNode.children.first.children.first.attribute as String] =
                Pair(defResult.cfgIn!!, defResult.cfgOut!!)
        }

        val bodyResult = generateCFG(bodyNode, graph)

        return CFG(graph, bodyResult.cfgIn, bodyResult.cfgOut)
    }

    private fun handleWhileNode(node: WhileNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val condition = node.children.filterIsInstance<CondNode>().first()
        val expression = node.children.filterIsInstance<ExprNode>().first()

        val condResult = generateCFG(condition, graph)
        val expResult = generateCFG(expression, graph)

        val diamond = CFGNode(node, "diamond")
        val glue = CFGNode(node, "glue")
        graph.addVertex(diamond)
        graph.addVertex(glue)

        Graphs.addGraph(graph, condResult.graph)
        Graphs.addGraph(graph, expResult.graph)

        //This edge seems wrong... but is listed in the slides on page 7 ??
        //graph.addEdge(condResult.cfgOut, expResult.cfgIn)

        graph.addEdge(condResult.cfgOut, diamond)
        graph.addEdge(diamond, expResult.cfgIn, Edge("T"))
        graph.addEdge(diamond, glue, Edge("F"))
        graph.addEdge(expResult.cfgOut, condResult.cfgIn)

        return CFG(graph, condResult.cfgIn, glue)
    }

    //TODO fix wrong paths
    private fun handleIfNode(node: IfNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val condition = generateCFG(node.children.filterIsInstance<CondNode>().first(), graph)
        val thenNode = generateCFG(node.children.filterIsInstance<ThenNode>().first(), graph)
        val elseNode = generateCFG(node.children.filterIsInstance<ElseNode>().first(), graph)
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

        return CFG(graph, condition.cfgIn, glue)
    }

    private fun handleAssignNode(node: AssignNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val result1 = generateCFG(node.children[0], graph)
        Graphs.addGraph(graph, result1.graph)
        val label = node.children.filterIsInstance<IDNode>().first().attribute
        val cfgNode = CFGNode(node, "$label = e")
        graph.addVertex(cfgNode)
        graph.addEdge(result1.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)
    }

    private fun handleSemiNode(node: SemiNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val result1 = generateCFG(node.children[0], graph)
        val result2 = generateCFG(node.children[1], graph)
        Graphs.addGraph(graph, result1.graph)
        Graphs.addGraph(graph, result2.graph)
        graph.addEdge(result1.cfgOut, result2.cfgIn)

        var nextCFG: CFG
        var lastCFG = result2
        node.children.filterIndexed { index, _ -> index >= 2 }.forEach { nextNode ->
            nextCFG = generateCFG(nextNode, graph)
            Graphs.addGraph(graph, nextCFG.graph)
            graph.addEdge(lastCFG.cfgOut, nextCFG.cfgIn)
            lastCFG = nextCFG
        }
        return CFG(graph, result1.cfgIn, lastCFG.cfgOut)

    }

    private fun handleOptNode(node: OpNode, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val result1 = generateCFG(node.children[0], graph)
        val result2 = generateCFG(node.children[1], graph)

        Graphs.addGraph(graph, result1.graph)
        Graphs.addGraph(graph, result2.graph)

        val label = node.attribute.toString()
        val cfgNode = CFGNode(node, label)

        graph.addVertex(cfgNode)
        graph.addEdge(result1.cfgOut, result2.cfgIn)
        graph.addEdge(result2.cfgOut, cfgNode)

        return CFG(graph, result1.cfgIn, cfgNode)
    }

    private fun handleConstOrIDNode(node: Node, graph: SimpleDirectedGraph<CFGNode, Edge>): CFG {
        val cfgNode = CFGNode(node, node.attribute.toString())
        graph.addVertex(cfgNode)
        return CFG(graph, cfgNode, cfgNode)
    }
}
