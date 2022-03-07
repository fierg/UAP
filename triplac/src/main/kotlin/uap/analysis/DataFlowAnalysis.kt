package uap.analysis

import org.jgrapht.traverse.DepthFirstIterator
import uap.cfg.CFG
import uap.node.IDNode
import uap.node.ParamsNode

class DataFlowAnalysis {
    companion object {
        fun analyzeLiveVariables(cfgGraph: CFG) {
            initGenAndKill(cfgGraph)

        }

        private fun initGenAndKill(cfgGraph: CFG) {
            DepthFirstIterator(cfgGraph.graph).forEachRemaining { currentNode ->
                println("${currentNode.node.type} ${currentNode.label}")

                when(currentNode.node.type) {
                    "ID" -> currentNode.gen.add(currentNode.label)
                    "ASSIGN" -> currentNode.kill.add(currentNode.node.children.filterIsInstance<IDNode>().first().attribute as String)
                    "FUNC" -> currentNode.kill.addAll(currentNode.node.children.filterIsInstance<ParamsNode>().first().children.filterIsInstance<IDNode>().map { it.attribute as String })
                }
            }
        }
    }
}