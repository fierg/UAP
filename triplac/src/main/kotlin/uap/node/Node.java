package uap.node;

import uap.flattener.IFlattener;

import java.util.LinkedList;

public abstract class Node implements IAcceptor
{
    private String type;
    private Object attribute;
    private LinkedList<Node> children;

    public Node(String type)
    {
        this.type = type;
        this.attribute = null;
        this.children = new LinkedList<>();
    }

    public Node(String type, Object attribute)
    {
        this.type = type;
        this.attribute = attribute;
        this.children = new LinkedList<>();
    }

    public String getType()
    {
        return this.type;
    }

    public Object getAttribute()
    {
        return this.attribute;
    }

    public LinkedList<Node> getChildren()
    {
        return this.children;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setAttribute(String attribute)
    {
        this.attribute = attribute;
    }

    public void setChildren(LinkedList<Node> children)
    {
        this.children = children;
    }

    public void addChild(Node child)
    {
        this.children.add(child);
    }

    public void addChildren(LinkedList<Node> children)
    {
        this.children.addAll(children);
    }

    private String startTag()
    {
        String tag = "<" + type;

        if (attribute != null)
        {
            tag += " attr=\"" + attribute + "\"";
        }

        tag += ">";

        return tag;
    }

    private String endTag()
    {
        return "</" + type + ">";
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder(startTag());
        for (Node node : children)
        {
            str.append(node.toString());
        }
        str.append(endTag());
        return str.toString();
    }

    @Override
    public void accept(IFlattener v){
        v.visit(this);
    }
}
