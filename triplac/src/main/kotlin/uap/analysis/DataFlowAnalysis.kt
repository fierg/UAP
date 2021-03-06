package uap.analysis

import org.jgrapht.Graphs
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.BreadthFirstIterator
import org.jgrapht.traverse.DepthFirstIterator
import uap.cfg.CFG
import uap.cfg.iterator.CFGIterator
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.export.DOTWriter
import uap.node.*

class DataFlowAnalysis {
    companion object {
        fun analyzeLiveVariables(cfgGraph: CFG, export: Boolean) {
            initGenAndKill(cfgGraph)
            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)
            handleExport(export, cfgGraph, "CFG after initialization:")

            //repeat until no further changes are made
            var updatePerformed: Boolean
            do {
                updatePerformed = false
                DepthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                    updatePerformed = updateLiveVarsOfNode(invertedGraph, currentNode) || updatePerformed
                }
                if (updatePerformed) println("changes detected, performing another iteration...")
            } while (updatePerformed)
            println("done.")
            handleExport(export,cfgGraph,"CFG after finding a fix point:")
        }

        fun analyzeReachedUses(cfgGraph: CFG, export: Boolean){
            //val map = CFGIterator(cfgGraph).list().associate { it.second to it.first }.toMutableMap()
            //cfgGraph.graph.vertexSet().forEach { map.computeIfAbsent(it) {"_"} }
            initGenAndKillReachedUses(cfgGraph)
            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)
            handleExport(export, cfgGraph, "CFG after initialization:")

            //repeat until no further changes are made
            var updatePerformed: Boolean
            do {
                updatePerformed = false
                BreadthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                    updatePerformed = updateReachedUsesOfNode(invertedGraph, currentNode) || updatePerformed
                }
                if (updatePerformed) println("changes detected, performing another iteration...")
            } while (updatePerformed)
            println("done.")

            println("Adding data flow edges to CFG")
            DepthFirstIterator(cfgGraph.graph,cfgGraph.cfgIn).forEach {
                when(it.node) {
                    is AssignNode -> handleAssignNodeEdges(it, cfgGraph)
                    is FuncNode -> handleFuncNodeEdges(it,cfgGraph)
                }
            }

            handleExport(export,cfgGraph,"CFG after finding a fix point:")
        }

        private fun handleFuncNodeEdges(node: CFGNode, cfgGraph: CFG) {
            if (node.label.startsWith("START")) {
                val variables = node.ruKillSet.map { it.second }
                val uses = node.ruOutSet.filter { variables.contains(it.second) }

                uses.forEach {
                    cfgGraph.graph.addEdge(node, it.first,Edge("dataflow"))
                }
            }
        }

        private fun handleAssignNodeEdges(node: CFGNode, cfgGraph: CFG) {
            val variable = node.ruKillSet.first().second
            val uses = node.ruOutSet.filter { it.second == variable }

            uses.forEach {
                cfgGraph.graph.addEdge(node, it.first,Edge("dataflow"))
            }

            if (uses.isEmpty()) {
                println("No usage of assignment found for node ${node.node.type} ${node.label}! Can be removed during optimization.")
                node.removeable = true
            }
        }

        private fun updateReachedUsesOfNode(
            invertedGraph: EdgeReversedGraph<CFGNode, Edge>,
            currentNode: CFGNode
        ): Boolean {
            var updated = false
            val oldOut = currentNode.ruOutSet.toSet()
            Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                currentNode.ruOutSet.addAll(it.ruInSet)
            }
            val newInSet = currentNode.ruOutSet.toMutableSet()
            newInSet.removeIf { node -> currentNode.ruKillSet.map { it.second }.contains(node.second) }
            newInSet.addAll(currentNode.ruGenSet)

            if (currentNode.ruInSet != newInSet || oldOut != currentNode.ruOutSet){
                updated = true
            }
            currentNode.ruInSet = newInSet

            return updated
        }

        private fun updateLiveVarsOfNode(invertedGraph: EdgeReversedGraph<CFGNode, Edge>, currentNode: CFGNode): Boolean {
            var updated = false
            val oldOut = currentNode.outSet.toSet()
            Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                currentNode.outSet.addAll(it.inSet)
            }
            val newInSet = currentNode.outSet.toMutableSet()
            newInSet.removeAll(currentNode.kill)
            newInSet.addAll(currentNode.gen)

            if (currentNode.inSet != newInSet || oldOut != currentNode.outSet){
                println("Updated ${currentNode.node.type}:${currentNode.label}     in: (${currentNode.inSet}) -> ($newInSet)        out: (${oldOut}) -> (${currentNode.outSet})")
                updated = true
            }
            currentNode.inSet = newInSet

            return updated
        }

        private fun initGenAndKill(cfgGraph: CFG) {
            DepthFirstIterator(cfgGraph.graph, cfgGraph.cfgIn).forEach { currentNode ->
                when(currentNode.node.type) {
                    "ID" -> currentNode.gen.add(currentNode.label)
                    "ASSIGN" -> currentNode.kill.add(currentNode.node.children.filterIsInstance<IDNode>().first().attribute as String)
                    "FUNC" -> currentNode.kill.addAll(currentNode.node.children.filterIsInstance<ParamsNode>().first().children.filterIsInstance<IDNode>().map { it.attribute as String })
                }
                if (currentNode.gen.isNotEmpty())
                    currentNode.inSet = currentNode.gen.toMutableSet()
            }
        }

        private fun initGenAndKillReachedUses(cfgGraph: CFG) {
            DepthFirstIterator(cfgGraph.graph, cfgGraph.cfgIn).forEach { currentNode ->
                when(currentNode.node.type) {
                    "ID" -> currentNode.ruGenSet.add(Pair(currentNode, currentNode.label))
                    "ASSIGN" -> currentNode.ruKillSet.add(Pair(currentNode, currentNode.node.children.filterIsInstance<IDNode>().first().attribute as String))
                    "FUNC" -> currentNode.ruKillSet.addAll(currentNode.node.children.filterIsInstance<ParamsNode>().first().children.filterIsInstance<IDNode>().map {Pair(currentNode, it.attribute as String)})
                }
                if (currentNode.ruGenSet.isNotEmpty())
                    currentNode.ruInSet = currentNode.ruGenSet.toMutableSet()
            }
        }

        private fun handleExport(export: Boolean, cfgGraph: CFG, message: String) {
            if (export) {
                println(message)
                DOTWriter.exportGraph(cfgGraph)
            }
        }

        private fun findNodesInAST(cfgGraph: CFG, ast: Node, predecessor: Node? = null): MutableList<Triple<Node, Node, Node>> {
            val target = mutableListOf<Triple<Node,Node,Node>>()
            if (cfgGraph.graph.vertexSet().find { it.node == ast }?.removeable == true){
                target.add(Triple(ast, predecessor!!, ast.children.last))
                ast.children.forEach { target.addAll(findNodesInAST(cfgGraph,it,ast))}
            } else {
                ast.children.forEach { target.addAll(findNodesInAST(cfgGraph,it,ast))}
            }
            return target
        }


        fun optimize(cfgGraph: CFG, ast: Node){
            val nodes = findNodesInAST(cfgGraph,ast)
            nodes.forEach {
                val pre = it.second
                val post = it.third
                val unwanted = pre.children.first { node -> node == it.first }
                pre.children.remove(unwanted)
                pre.children.add(post)
            }
        }
    }
}