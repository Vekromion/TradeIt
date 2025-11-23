package edu.uga.cs.tradeit.models;

public class Transaction {
    public String id, itemId, itemName, categoryID, buyerUserID, sellerUserID;
    public long createdAt;
    public long completedAt;

    public boolean itemIsFree;
    public Integer itemPrice;

}
