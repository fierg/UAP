package uap.flattener;

import uap.node.*;

import java.util.ListIterator;

public class Flattener {

    private void applyFlatten(Node node){
        boolean flattened = true;
        while(flattened){
            flattened = false;
            for (final ListIterator<Node> nodeIterator = node.getChildren().listIterator(); nodeIterator.hasNext();) {
                final Node currentChildren = nodeIterator.next();

                //If the children of current node are of the same type as the node, they will be removed and added as children of the current node
                if (node.getType().equals(currentChildren.getType())) {
                    nodeIterator.remove();
                    for (Node childrenOfRemovedNode:currentChildren.getChildren()) {
                        nodeIterator.add(childrenOfRemovedNode);
                    }
                    flattened = true;
                }
            }
        }
    }

    public void flatten(Node node) {
        //If node is of specified type, flatten() will be called
        if (node instanceof DefNode || node instanceof ArgsNode || node instanceof ParamsNode || node instanceof SemiNode) {
            applyFlatten(node);
            for (Node n:node.getChildren()) {
                flatten(n);
            }
        } else
            for (Node n:node.getChildren()) {
                flatten(n);
            }
    }

}