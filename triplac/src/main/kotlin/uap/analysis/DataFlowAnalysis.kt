package uap.analysis

import org.jgrapht.Graphs
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.DepthFirstIterator
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.export.DOTWriter
import uap.node.AssignNode
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
            initGenAndKillReachedUses(cfgGraph)
            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)
            handleExport(export, cfgGraph, "CFG after initialization:")

            //repeat until no further changes are made
            var updatePerformed: Boolean
            do {
                updatePerformed = false
                DepthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                    updatePerformed = updateReachedUsesOfNode(invertedGraph, currentNode) || updatePerformed
                }
                if (updatePerformed) println("changes detected, performing another iteration...")
            } while (updatePerformed)
            println("done.")

            println("Adding data flow edges to CFG")
            DepthFirstIterator(cfgGraph.graph,cfgGraph.cfgIn).forEach {
                when(it.node) {
                    is AssignNode -> handleAssignNodeEdge(it, cfgGraph)
                }
            }

            handleExport(export,cfgGraph,"CFG after finding a fix point:")
        }

        private fun handleAssignNodeEdge(node: CFGNode, cfgGraph: CFG) {
            val variable = node.ruKillSet.first().second
            val uses = node.ruOutSet.filter { it.second == variable }

            uses.forEach {
                cfgGraph.graph.addEdge(node, it.first,Edge("dataflow"))
            }
        }

        private fun updateReachedUsesOfNode(invertedGraph: EdgeReversedGraph<CFGNode, Edge>, currentNode: CFGNode): Boolean {
            var updated = false
            val oldOut = currentNode.ruOutSet.toSet()
            Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                currentNode.ruOutSet.addAll(it.ruInSet)
            }
            val newInSet = currentNode.ruOutSet.toMutableSet()
            newInSet.removeAll(currentNode.ruKillSet)
            newInSet.addAll(currentNode.ruGenSet)

            if (currentNode.ruInSet != newInSet || oldOut != currentNode.ruOutSet){
                println("Updated ${currentNode.node.type}:${currentNode.label}     in: (${currentNode.ruInSet}) -> ($newInSet)        out: (${oldOut}) -> (${currentNode.ruOutSet})")
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
                    "FUNC" -> currentNode.ruKillSet.addAll(currentNode.node.children.filterIsInstance<ParamsNode>().first().children.filterIsInstance<IDNode>().map {Pair(currentNode, it.attribute as String )})
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