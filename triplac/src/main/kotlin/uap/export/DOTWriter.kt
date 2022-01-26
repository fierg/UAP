package uap.export

import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.nio.dot.DOTExporter
import uap.cfg.CFGNode
import uap.cfg.Edge
import java.io.StringWriter

class DOTWriter {
    companion object {

        fun exportGraph(graph: SimpleDirectedGraph<CFGNode, Edge>) {
            //Create the exporter (with ID provider)
            val exporter2 =
                DOTExporter<CFGNode, Edge> { v: CFGNode -> "${v.node.type}${v.node.hashCode()}" }
            val writer = StringWriter()
            exporter2.exportGraph(graph, writer)
            println(writer.toString())
        }
    }
}