package com.lexmark.dev.FoIP.FoIPLicenseSales;

public class PurchaseOrder implements Comparable<PurchaseOrder> {
    public String ID;
    public int Units;
    public int EntitlementUnits;

    public PurchaseOrder() {
    };

    @Override
    public int compareTo (PurchaseOrder PO){
        return (this.ID.compareTo(PO.ID));
    }
}
