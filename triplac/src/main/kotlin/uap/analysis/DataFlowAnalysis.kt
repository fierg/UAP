package uap.analysis

import org.jgrapht.Graphs
import org.jgrapht.graph.EdgeReversedGraph
import org.jgrapht.traverse.BreadthFirstIterator
import org.jgrapht.traverse.DepthFirstIterator
import uap.cfg.CFG
import uap.node.IDNode
import uap.node.ParamsNode

class DataFlowAnalysis {
    companion object {
        fun analyzeLiveVariables(cfgGraph: CFG) {
            initGenAndKill(cfgGraph)

            val invertedGraph = EdgeReversedGraph(cfgGraph.graph)
            BreadthFirstIterator(invertedGraph,cfgGraph.cfgOut).forEach { currentNode ->
                //println("${currentNode.node.type} ${currentNode.label} ${currentNode.inSet} ${currentNode.outSet}")
                Graphs.predecessorListOf(invertedGraph, currentNode).forEach {
                    currentNode.outSet.addAll(it.inSet)
                }
                val newOutSet = currentNode.outSet.toMutableSet()
                newOutSet.removeAll(currentNode.kill)
                newOutSet.addAll(currentNode.gen)
            }
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