package uap.cfg

import uap.node.Node

class CFGNode(var InNode: Node?, var OutNode: Node?, var node: Node, var label: String? = if (node.attribute is Int) (node.attribute as Int).toString() else "" ) {}