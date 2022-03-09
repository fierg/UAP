package uap.cfg

import uap.node.Node

class CFGNode(
    var node: Node,
    var label: String = "",
    var gen: MutableSet<String> = mutableSetOf(),
    var kill: MutableSet<String> = mutableSetOf(),
    var inSet: MutableSet<String> = mutableSetOf(),
    var outSet: MutableSet<String> = mutableSetOf(),
    var ruGenSet: MutableSet<Pair<CFGNode,String>> = mutableSetOf(),
    var ruKillSet: MutableSet<Pair<CFGNode,String>> = mutableSetOf(),
    var ruInSet: MutableSet<Pair<CFGNode,String>> = mutableSetOf(),
    var ruOutSet: MutableSet<Pair<CFGNode,String>> = mutableSetOf(),
    var marksSubCFG: Boolean = false
) {
    override fun toString(): String {
        return label.ifBlank { node.attribute as String? ?: node.type }
    }
}