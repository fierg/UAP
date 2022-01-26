package uap.cfg

import uap.node.Node

class CFGNode(var node: Node, var label: String = "") {
    override fun toString(): String {
        return label.ifBlank { node.attribute as String? ?: node.type }
    }
}