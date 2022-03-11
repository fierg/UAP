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
import uap.node.AssignNode
import uap.node.FuncNode
import uap.node.IDNode
import uap.node.ParamsNode

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
            val map = CFGIterator(cfgGraph).list().associate { it.second to it.first }.toMutableMap()
            cfgGraph.graph.vertexSet().forEach { map.computeIfAbsent(it) {"_"} }
            initGenAndKillReachedUses(cfgGraph,map)
            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)
            handleExport(export, cfgGraph, "CFG after initialization:")

            //repeat until no further changes are made
            var updatePerformed: Boolean
            do {
                updatePerformed = false
                BreadthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                    updatePerformed = updateReachedUsesOfNode(invertedGraph, currentNode,map) || updatePerformed
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
        }

        private fun updateReachedUsesOfNode(
            invertedGraph: EdgeReversedGraph<CFGNode, Edge>,
            currentNode: CFGNode,
            map: Map<CFGNode, String>
        ): Boolean {
            var updated = false
            val oldOut = currentNode.ruOutSet.toSet()
            Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                currentNode.ruOutSet.addAll(it.ruInSet)
            }
            val newInSet = currentNode.ruOutSet.toMutableSet()
            //TODO validate proper removal of subsets
            newInSet.removeIf { node -> currentNode.ruKillSet.map { it.second }.contains(node.second) }
            newInSet.addAll(currentNode.ruGenSet)

            if (currentNode.ruInSet != newInSet || oldOut != currentNode.ruOutSet){
                if (map[currentNode] != "_") {
                    println("Updated ${map[currentNode]}        out: (${oldOut.map { it.third to it.second }}) -> (${currentNode.ruOutSet.map { it.third to it.second }})        in: (${currentNode.ruInSet.map { it.third to it.second }}) -> (${newInSet.map { it.third to it.second }})")
                }
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

        private fun initGenAndKillReachedUses(cfgGraph: CFG, map: Map<CFGNode, String>) {
            DepthFirstIterator(cfgGraph.graph, cfgGraph.cfgIn).forEach { currentNode ->
                when(currentNode.node.type) {
                    "ID" -> currentNode.ruGenSet.add(Triple(currentNode, currentNode.label, map[currentNode]!!))
                    "ASSIGN" -> currentNode.ruKillSet.add(Triple(currentNode, currentNode.node.children.filterIsInstance<IDNode>().first().attribute as String, map[currentNode]!!))
                    "FUNC" -> currentNode.ruKillSet.addAll(currentNode.node.children.filterIsInstance<ParamsNode>().first().children.filterIsInstance<IDNode>().map {Triple(currentNode, it.attribute as String, map[currentNode]!!)})
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
    }
}