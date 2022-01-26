package uap.cfg

import org.jgrapht.graph.SimpleDirectedGraph

class CFG(val graph: SimpleDirectedGraph<CFGNode, Edge>, val cfgIn: CFGNode?, val cfgOut: CFGNode?) {}