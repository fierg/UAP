package uap.cfg

import org.jgrapht.Graphs
import uap.node.FuncNode

class CFGIterator(private val cfg: CFG) {
    private val list = mutableListOf<Pair<String, CFGNode>>()

    init {
        val currentNode = cfg.cfgOut!!
        var pos = 0
        list.add(Pair("_", currentNode))

        while (currentNode != cfg.cfgIn) {
            when (currentNode.node) {
                is FuncNode -> {
                    val predecessors = Graphs.predecessorListOf(cfg.graph, currentNode)
                }
                 else -> {
                     val predecessors = Graphs.predecessorListOf(cfg.graph, currentNode)
                 }
            }
        }
    }

        fun forEach(action: (Pair<String,CFGNode>) -> Unit) {
            this.list.forEach { action(it) }
        }
}