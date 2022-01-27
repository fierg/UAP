package uap.export

import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import uap.cfg.CFGNode
import uap.cfg.Edge
import java.io.StringWriter


class DOTWriter {
    companion object {

        fun exportGraph(graph: SimpleDirectedGraph<CFGNode, Edge>) {
            //Create the exporter (with ID provider)
            val exporter = DOTExporter<CFGNode, Edge>()
            exporter.setVertexAttributeProvider { v ->
                val map: MutableMap<String, Attribute> = LinkedHashMap()
                map["label"] = DefaultAttribute.createAttribute("${v.node.type}${v.node.hashCode()}")
                map
            }
            exporter.setEdgeAttributeProvider { e ->
                val map: MutableMap<String, Attribute> = LinkedHashMap()
                map["label"] = DefaultAttribute.createAttribute(e.label)
                map
            }
            val writer = StringWriter()
            exporter.exportGraph(graph, writer)
            println(writer.toString())
        }
    }
}