package uap.flattener

import uap.node.*

class Flattener {
    private fun applyFlatten(node: Node) {
        var flattened = true
        while (flattened) {
            flattened = false
            val nodeIterator = node.children.listIterator()
            while (nodeIterator.hasNext()) {
                val currentChildren = nodeIterator.next()

                //If the children of current node are of the same type as the node, they will be removed and added as children of the current node
                if (node.type == currentChildren.type) {
                    nodeIterator.remove()
                    for (childrenOfRemovedNode in currentChildren.children) {
                        nodeIterator.add(childrenOfRemovedNode)
                    }
                    flattened = true
                }
            }
        }
    }

    fun flatten(node: Node) {
        //If node is of specified type, flatten() will be called
        if (node is DefNode || node is ArgsNode || node is ParamsNode || node is SemiNode) {
            applyFlatten(node)
            for (n in node.children) {
                flatten(n)
            }
        } else for (n in node.children) {
            flatten(n)
        }
    }
}