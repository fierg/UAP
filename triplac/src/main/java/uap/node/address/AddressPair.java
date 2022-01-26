package uap.node.address;

public abstract class AddressPair {
    protected Object loc;
    protected int nl;

    public AddressPair(Object loc, int nl) {
        this.loc = loc;
        this.nl = nl;
    }

    public Object getLoc() {
        return loc;
    }
    public void setLoc(Object loc) {
        this.loc = loc;
    }
    public int getNl() {
        return nl;
    }
    public void setNl(int nl) {
        this.nl = nl;
    }
}
