package uap.cfg

import org.jgrapht.graph.DefaultEdge

class Edge(val label: String = "") : DefaultEdge() {
    override fun toString(): String {
        return label
    }
}