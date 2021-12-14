
package uap.node;

public class OpNode extends Node
{
    public static final String ADD  = "+";
    public static final String SUB  = "-";
    public static final String MULT = "*";
    public static final String DIV  = "/";
    public static final String EQ   = "==";
    public static final String NEQ  = "!=";
    public static final String LT   = "&lt;";
    public static final String GT   = "&gt;";
	public static final String GTE   = "&ge;";
	public static final String LTE   = "&le;";
    public static final String AND   = "&amp;&amp;";
    public static final String OR   = "||";

    public OpNode(String value)
    {
        super("OP", value);
    }
}
