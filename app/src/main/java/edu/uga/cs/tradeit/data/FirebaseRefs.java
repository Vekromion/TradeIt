package edu.uga.cs.tradeit.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Helper class for Firebase
 */
public class FirebaseRefs {
    private static final FirebaseDatabase db = FirebaseDatabase.getInstance();
    public static DatabaseReference users() {
        return db.getReference("users");
    }

    public static DatabaseReference categories() {
        return db.getReference("categories");
    }
    public static DatabaseReference itemsByCategory(String catId) {
        return db.getReference("itemsByCategory").child(catId);
    }
    public static DatabaseReference itemsByOwner(String userId) {
        return db.getReference("itemsByOwner").child(userId);
    }
    public static DatabaseReference pendingByUser(String userId) {
        return db.getReference("transactions").child("pendingByUser").child(userId);
    }
    public static DatabaseReference completedByUser(String userId) {
        return db.getReference("transactions").child("completedByUser").child(userId);
    }
    public static DatabaseReference root(){ return db.getReference(); }

}
