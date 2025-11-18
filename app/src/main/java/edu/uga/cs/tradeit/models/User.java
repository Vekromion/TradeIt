package edu.uga.cs.tradeit.models;

public class User {
    public String uid, email, displayName;
    public User() {}
    public User(String uid, String email, String displayName) {
        this.uid = uid; this.email = email; this.displayName = displayName;
    }
}
