package uap.cfg.iterator

import org.jgrapht.Graphs
import uap.cfg.CFG
import uap.cfg.CFGNode
import uap.node.*
import java.util.*

class CFGIterator(cfg: CFG) {
    private val list = mutableListOf<Pair<String, CFGNode>>()

    init {
        var currentNode = Graphs.successorListOf(cfg.graph, cfg.cfgIn!!).first()
        val nameSpace = ArrayDeque<Pair<String, Int>>()
        val secondPath = ArrayDeque<CFGNode>()
        val searchWhileLoop = ArrayDeque<Boolean>()

        var skippedCallNode = false
        nameSpace.push(Pair("_", 1))

        while (currentNode != cfg.cfgOut) {
            if (currentNode.node is ConstNode || currentNode.node is OpNode) {
                currentNode = Graphs.successorListOf(cfg.graph, currentNode).first()
            }  else if (currentNode.label == "glue") {
                currentNode = if (secondPath.isNotEmpty()) {
                    secondPath.pop()
                } else {
                    Graphs.successorListOf(cfg.graph, currentNode).first()
                }
            } else {
                if (searchWhileLoop.isNotEmpty() && list.map { it.second }.contains(currentNode)) {
                    currentNode = secondPath.pop()
                    searchWhileLoop.pop()
                }

                val pos = nameSpace.pop()
                val successors = Graphs.successorListOf(cfg.graph, currentNode)
                list.add(Pair("${pos.first}${pos.second}", currentNode))
                nameSpace.push(Pair(pos.first, pos.second + 1))

                when (currentNode.node) {
                    is CallNode -> {
                        if (currentNode.label == "CALL") {
                            val funcNode = Graphs.successorListOf(cfg.graph, currentNode).first { it.node is FuncNode }
                            val newNameSpace = Regex("START (.+?)\\[").find(funcNode.label)!!.groupValues[1]
                            currentNode = if (nameSpace.peek().first == newNameSpace) {
                                skippedCallNode = true
                                Graphs.successorListOf(cfg.graph, currentNode).first { it.node is CallNode }
                            } else {
                                nameSpace.push(Pair(newNameSpace, 1))
                                funcNode
                            }
                        } else {
                            if (skippedCallNode)
                                skippedCallNode = false

                            currentNode = successors.first()
                        }
                    }
                    is IfNode -> {
                        if (currentNode.label == "diamond") {
                            secondPath.push(successors.first())
                            currentNode = successors.last()
                        }
                    }
                    is WhileNode -> {
                        currentNode = successors.first{it.label != "glue"}
                        searchWhileLoop.push(true)
                        secondPath.push(Graphs.successorListOf(cfg.graph , successors.first{it.label == "glue"}).first())
                    }
                    is FuncNode -> {
                        currentNode = if (currentNode.label.startsWith("START"))
                            successors.first()
                        else {
                            nameSpace.pop()
                            successors.first { node -> !list.map { it.second }.contains(node) }
                        }
                    }
                    else -> currentNode = handleDefault(successors, currentNode)
                }
            }
        }
    }

    private fun handleDefault(
        successors: MutableList<CFGNode>,
        currentNode: CFGNode
    ): CFGNode {
        var currentNode1 = currentNode
        if (successors.size == 1)
            currentNode1 = successors.first()
        else
            throw IllegalArgumentException("Too many successors for node ${currentNode1.node.type} ${currentNode1.label}")
        return currentNode1
    }

    fun forEach(action: (Pair<String, CFGNode>) -> Unit) {
        this.list.forEach { action(it) }
    }

    fun list(): List<Pair<String, CFGNode>> {
        return list.toList()
    }
}