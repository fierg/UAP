package uap.export

import org.jgrapht.Graphs
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.nio.Attribute
import org.jgrapht.nio.DefaultAttribute
import org.jgrapht.nio.dot.DOTExporter
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.cfg.Edge
import uap.node.CallNode
import uap.node.ConstNode
import uap.node.IDNode
import java.io.StringWriter

class DOTWriter {
    companion object {

        fun exportGraph(cfgGraph: CFG) {
            //Create the exporter (with custom provider)
            val exporter = DOTExporter<CFGNode, Edge>()
            val debug = false
            val graph : SimpleDirectedGraph<CFGNode, Edge> = if (debug){
                addInfoNodesToGraph(cfgGraph)
            } else {
                cfgGraph.graph
            }

            exporter.setVertexAttributeProvider { v ->
                val map: MutableMap<String, Attribute> = LinkedHashMap()
                val label = if (v.label.isNotBlank()) v.label else if (v.node.attribute != null) v.node.attribute.toString() else throw IllegalArgumentException("Unhandled node type: $v")

                when {
                    (v == cfgGraph.cfgIn || v == cfgGraph.cfgOut) -> {
                        map["shape"] = DefaultAttribute.createAttribute("doublecircle")
                        map["label"] = DefaultAttribute.createAttribute(label)
                    }
                    (label == "diamond") -> {
                        map["shape"] = DefaultAttribute.createAttribute(label)
                        map["label"] = DefaultAttribute.createAttribute("?")
                    }
                    (label == "glue") -> {
                        map["shape"] = DefaultAttribute.createAttribute("point")
                        map["label"] = DefaultAttribute.createAttribute("")
                    }
                    (label.startsWith("START") || label.startsWith("END") || label.startsWith("CALL") || label.startsWith(
                        "RET"
                    )) -> {
                        map["shape"] = DefaultAttribute.createAttribute("box")
                        map["label"] = DefaultAttribute.createAttribute(label)
                    }
                    (label.startsWith("G: ")) -> {
                        map["shape"] = DefaultAttribute.createAttribute("note")
                        map["label"] = DefaultAttribute.createAttribute(label)
                        map["color"] = DefaultAttribute.createAttribute("green")
                    }
                    else -> {
                        map["label"] = DefaultAttribute.createAttribute(label)
                    }
                }
                map
            }

            exporter.setEdgeAttributeProvider { e ->
                val map: MutableMap<String, Attribute> = LinkedHashMap()
                when (e.label) {
                    "undirected" -> {
                        map["arrowhead"] = DefaultAttribute.createAttribute("none")
                        map["label"] = DefaultAttribute.createAttribute("")
                    }
                    "dataflow" -> {
                        map["color"] = DefaultAttribute.createAttribute("green")
                    }
                    else -> map["label"] = DefaultAttribute.createAttribute(e.label)
                }
                map
            }

            val writer = StringWriter()
            exporter.exportGraph(graph, writer)
            println(writer.toString())
        }

        private fun addInfoNodesToGraph(cfgGraph: CFG): SimpleDirectedGraph<CFGNode, Edge> {
            val infoNodes = mutableSetOf<Pair<CFGNode, CFGNode>>()
            val graph = SimpleDirectedGraph<CFGNode,Edge>(Edge::class.java)
            Graphs.addGraph(graph,cfgGraph.graph)

            cfgGraph.graph.vertexSet().filterNot { it.node is ConstNode || it.label == "glue" || it.node is CallNode || it == cfgGraph.cfgIn }.forEachIndexed { _, v ->
                if (v.gen.isNotEmpty() || v.kill.isNotEmpty() || v.inSet.isNotEmpty() || v.outSet.isNotEmpty()) {
                    val analysisNode = CFGNode(v.node, label = "G: ${v.gen.toString().replace(Regex("\\[|\\]"), "")} | K: ${v.kill.toString().replace(Regex("\\[|\\]"), "")} | I: ${v.inSet.toString().replace(Regex("\\[|\\]"), "")} | O: ${v.outSet.toString().replace(Regex("\\[|\\]"), "")}")
                    infoNodes.add(Pair(analysisNode, v))
                }
                if (v.ruGenSet.isNotEmpty() || v.ruKillSet.isNotEmpty() || v.ruInSet.isNotEmpty() || v.ruOutSet.isNotEmpty()) {
                    val analysisNode = CFGNode(v.node, label = "G: ${v.ruGenSet.toString().replace(Regex("\\[|\\]"), "")} | K: ${v.ruKillSet.map { it.second }.toString().replace(Regex("\\[|\\]"), "")} | I: ${v.ruInSet.toString().replace(Regex("\\[|\\]"), "")} | O: ${v.ruOutSet.toString().replace(Regex("\\[|\\]"), "")}")
                    infoNodes.add(Pair(analysisNode, v))
                }
            }
            infoNodes.forEach { pair ->
                graph.addVertex(pair.first)
                graph.addEdge(pair.first, pair.second, Edge("undirected"))
            }
            return graph
        }
    }
}