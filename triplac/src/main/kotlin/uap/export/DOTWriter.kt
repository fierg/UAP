package uap.export

import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import java.io.StringWriter


class DOTWriter {
    companion object {

        fun exportGraph(cfgGraph: CFG) {
            val graph = cfgGraph.graph
            //Create the exporter (with custom provider)
            val exporter = DOTExporter<CFGNode, Edge>()
            exporter.setVertexAttributeProvider { v ->
                val map: MutableMap<String, Attribute> = LinkedHashMap()
                val label = if (v.label.isNotBlank()) v.label else if (v.node.attribute != null) v.node.attribute.toString() else throw IllegalArgumentException("Unhandled node type: $v")

                if (v == cfgGraph.cfgIn || v == cfgGraph.cfgOut){
                    map["shape"] = DefaultAttribute.createAttribute("doublecircle")
                    map["label"] = DefaultAttribute.createAttribute(label)
                } else if (label == "diamond") {
                    map["shape"] = DefaultAttribute.createAttribute(label)
                    map["label"] = DefaultAttribute.createAttribute("?")
                } else if (label == "glue") {
                    map["label"] = DefaultAttribute.createAttribute("")
                    map["shape"] = DefaultAttribute.createAttribute("point")
                } else if (label.startsWith("START") || label.startsWith("END") || label.startsWith("CALL") || label.startsWith("RET")) {
                    map["label"] = DefaultAttribute.createAttribute(label)
                    map["shape"] = DefaultAttribute.createAttribute("box")
                } else {
                    map["label"] = DefaultAttribute.createAttribute(label)
                }
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