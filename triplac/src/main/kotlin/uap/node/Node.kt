package uap.node

import java.util.*

abstract class Node {
    var type: String
    var attribute: Any?
        private set
    private var children: LinkedList<Node>

    constructor(type: String) {
        this.type = type
        attribute = null
        children = LinkedList<Node>()
    }

    constructor(type: String, attribute: Any?) {
        this.type = type
        this.attribute = attribute
        children = LinkedList<Node>()
    }

    fun getChildren(): LinkedList<Node> {
        return children
    }

    fun setAttribute(attribute: String?) {
        this.attribute = attribute
    }

    fun setChildren(children: LinkedList<Node>) {
        this.children = children
    }

    fun addChild(child: Node) {
        children.add(child)
    }

    fun addChildren(children: LinkedList<Node>?) {
        this.children.addAll(children!!)
    }

    private fun startTag(): String {
        var tag = "<$type"
        if (attribute != null) {
            tag += " attr=\"$attribute\""
        }
        tag += ">"
        return tag
    }

    private fun endTag(): String {
        return "</$type>"
    }

    override fun toString(): String {
        val str = StringBuilder(startTag())
        for (node in children) {
            str.append(node.toString())
        }
        str.append(endTag())
        return str.toString()
    }
}