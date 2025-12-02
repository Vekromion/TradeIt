package edu.uga.cs.tradeit.models;

public class Transaction {
    public String id, itemId, itemName, categoryID, buyerUserID, sellerUserID, status, buyerName, sellerName;
    public long createdAt;
    public long completedAt;

    public boolean itemIsFree, buyerConfirmed, sellerConfirmed;
    public Integer itemPrice;

}
