package uap.cfg

import uap.node.Node

class CFGNode(
    var node: Node,
    var label: String = "",
    var gen: MutableSet<String> = mutableSetOf(),
    var kill: MutableSet<String> = mutableSetOf(),
    var inSet: MutableSet<String> = mutableSetOf(),
    var outSet: MutableSet<String> = mutableSetOf()
) {
    override fun toString(): String {
        return label.ifBlank { node.attribute as String? ?: node.type }
    }
}