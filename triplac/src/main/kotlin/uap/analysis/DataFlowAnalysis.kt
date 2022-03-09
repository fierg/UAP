package uap.analysis

import org.jgrapht.Graphs
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.BreadthFirstIterator
import org.jgrapht.traverse.DepthFirstIterator
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.export.DOTWriter
import uap.node.IDNode
import uap.node.ParamsNode

class DataFlowAnalysis {
    companion object {
        fun analyzeLiveVariables(cfgGraph: CFG, export: Boolean) {
            initGenAndKill(cfgGraph)
            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)

            if (export) {
                println("CFG after initialization:")
                DOTWriter.exportGraph(cfgGraph)
            }

            //Initial round
            BreadthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                updateNode(invertedGraph, currentNode)
            }

            if (export) {
                println("CFG after first round:")
                DOTWriter.exportGraph(cfgGraph)
            }

            //repeat until no further changes are made
            var updatePerformed: Boolean
            do {
                updatePerformed = false
                BreadthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                    updatePerformed = updateNode(invertedGraph, currentNode) || updatePerformed
                }
                println("changes happened, performing another iteration...")
            } while (updatePerformed)
            println("done.")

            if (export) {
                println("CFG after finding a fix point:")
                DOTWriter.exportGraph(cfgGraph)
            }
        }

        private fun updateNode(invertedGraph: EdgeReversedGraph<CFGNode, Edge>, currentNode: CFGNode): Boolean {
            var updated = false
            val oldOut = currentNode.outSet.toSet()
            Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                currentNode.outSet.addAll(it.inSet)
            }
            val newInSet = currentNode.outSet.toMutableSet()
            newInSet.removeAll(currentNode.kill)
            newInSet.addAll(currentNode.gen)

            if (currentNode.inSet != newInSet && oldOut != currentNode.outSet){
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
    }
}