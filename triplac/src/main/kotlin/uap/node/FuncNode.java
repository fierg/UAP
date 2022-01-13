
package uap.node;

import kotlin.Pair;
import uap.Instruction;
import uap.node.address.AddressPair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class FuncNode extends Node {
    public FuncNode() {
        super("FUNC");
    }

    @Override
    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        System.out.println("Handling func node on level " + nl);

        Map<String, AddressPair> rhoC = new HashMap(rho);
        AtomicReference<String> functionName = new AtomicReference<>("");
        List<String> params = new LinkedList();

        getChildren().forEach(node -> {
            if (IDNode.class.equals(node.getClass())) {
                functionName.set((String) node.getAttribute());
            } else if (ParamsNode.class.equals(node.getClass())) {
                node.getChildren().forEach(innerNode -> {
                    if (IDNode.class.equals(innerNode.getClass())) {
                        params.add((String) innerNode.getAttribute());
                    }
                });

                //TODO handle rho, increment nesting level and create rho' ??
            } else if (BodyNode.class.equals(node.getClass())) {
                rhoC.put(functionName.get(), addressFactory.getNewLabelAddressPair(-1, nl));
                params.forEach(param -> rhoC.put(param, addressFactory.getNewLabelAddressPair(-1, nl)));

                node.getChildren().forEach(innerNode -> rhoC.putAll(node.elab_def(rhoC, nl + 1)));
            }
        });

        return rhoC;
    }
}
