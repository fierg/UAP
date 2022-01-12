package uap.flattener;


import uap.node.*;

import java.util.ListIterator;

public class Flattener implements IFlattener {

    private void flatten(Node node){
        boolean flattened = true;
        while(flattened){
            flattened = false;
            for (final ListIterator<Node> i = node.getChildren().listIterator(); i.hasNext();) {
                final Node n = i.next();
                if (node.getType().equals(n.getType())) {
                    i.remove();
                    for (Node m:n.getChildren()) {
                        i.add(m);
                    }
                    flattened = true;
                }
            }
        }
    }

    protected void permute(Node node){
        for (Node n:node.getChildren()) {
            n.accept(this);
        }
    }

    @Override
    public void visit(Node node) {
        if (node instanceof ArgsNode || node instanceof DefNode || node instanceof ParamsNode || node instanceof SemiNode) {
            flatten(node);
            permute(node);
        } else
            permute(node);
    }

}
