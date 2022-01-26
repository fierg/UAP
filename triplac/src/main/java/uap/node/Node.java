package uap.node;

import uap.node.address.AddressFactory;
import uap.node.address.AddressPair;
import uap.node.interfaces.ITramcodeGeneratable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Node implements ITramcodeGeneratable {
    private String type;
    private Object attribute;
    private LinkedList<Node> children;
    static final AddressFactory addressFactory = new AddressFactory();

    public Node(String type) {
        this.type = type;
        this.attribute = null;
        this.children = new LinkedList<>();
    }

    public Node(String type, Object attribute) {
        this.type = type;
        this.attribute = attribute;
        this.children = new LinkedList<>();
    }

    public String getType() {
        return this.type;
    }

    public Object getAttribute() {
        return this.attribute;
    }

    public LinkedList<Node> getChildren() {
        return this.children;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setChildren(LinkedList<Node> children) {
        this.children = children;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public void addChildren(LinkedList<Node> children) {
        this.children.addAll(children);
    }

    private String startTag() {
        String tag = "<" + type;

        if (attribute != null) {
            tag += " attr=\"" + attribute + "\"";
        }

        tag += ">";

        return tag;
    }

    private String endTag() {
        return "</" + type + ">";
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(startTag());
        for (Node node : children) {
            str.append(node.toString());
        }
        str.append(endTag());
        return str.toString();
    }

    public Map<String, AddressPair> elab_def(Map<String, AddressPair> rho, int nl) {
        AtomicReference<Map<String, AddressPair>> rhoC = new AtomicReference<>(new HashMap<>(rho));

        if (!children.isEmpty())
            //System.out.println("Handling default node: " + this.type + " on level " + nl);
            children.forEach(child -> rhoC.set(child.elab_def(rhoC.get(), nl)));
        //System.out.println(rho.toString());
        return rhoC.get();
    }

    /*
    public List<Instruction> code(Map<String, AddressPair> rho, int nl) {
        LinkedList<Instruction> inst = new LinkedList<>();
        if (!children.isEmpty())
            //System.out.println("coding default node: " + this.type + " on level " + nl);
            children.forEach(child -> {
                List<Instruction> inst1 = child.code(rho, nl);
                if (inst1 != null && !inst1.isEmpty()){
                    inst.addAll(inst1);
                }
            });
        return inst;
    }
     */
}
