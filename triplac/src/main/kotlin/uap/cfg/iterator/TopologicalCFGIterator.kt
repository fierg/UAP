package uap.cfg.iterator

import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleDirectedGraph
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge

class TopologicalCFGIterator(private val cfg: CFG) {

    fun performTopologicalSort(){
        val acyclicCFG = SimpleDirectedGraph<CFGNode, Edge>(Edge::class.java)
        Graphs.addGraph(acyclicCFG,cfg.graph)

    }
}