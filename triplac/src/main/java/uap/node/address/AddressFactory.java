package uap.node.address;

public class AddressFactory {
    private static int IntegerLoc = 0;

    public IntegerAddressPair getNewIntegerAddressPair(int nl){
        return new IntegerAddressPair(IntegerLoc++,nl);
    }

    public LabelAddressPair getNewLabelAddressPair(int address, int nl){
        return new LabelAddressPair(new TramLabel(address) ,nl);
    }

}
