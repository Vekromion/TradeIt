package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.uga.cs.tradeit.data.FirebaseRefs;
import edu.uga.cs.tradeit.models.Category;
import edu.uga.cs.tradeit.models.Item;
import edu.uga.cs.tradeit.models.Transaction;

/**
 * Contains categories, items, and their interactions
 */
public class CategoriesActivity extends AppCompatActivity implements RecyclerAdapter.OnRowAction {

    public static final String EXTRA_MODE = "mode" ;
    public static final String EXTRA_CATEGORY_ID = "categoryId";
    public static final String EXTRA_CATEGORY_NAME = "categoryName";
    public static final int MODE_CATEGORIES = 0;
    public static final int MODE_ITEMS = 1;

    private int mode;
    private String categoryId, categoryName;
    private RecyclerView rcView;
    private RecyclerAdapter adapter;
    private ValueEventListener catListener, itemListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar
        Toolbar toolbar = findViewById( R.id.toolbar2 );
        setSupportActionBar(toolbar);

        // Back and Home on Toolbar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(this));

        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_CATEGORIES);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        setTitle(mode == MODE_CATEGORIES ? "Categories" : ("Items in " + categoryName));

        adapter = new RecyclerAdapter(this, mode);
        if (mode == MODE_ITEMS) {
            adapter.setCurrentCategoryName(categoryName);
        }
        rcView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            if (mode==MODE_CATEGORIES) showAddCategory();
            else showAddItem();
        });
    }
    // Connects Page to Firebase
    @Override protected void onStart() {
        super.onStart();
        // Deals with the thing to be displayed
        if (mode == MODE_CATEGORIES) {
            // Display categories
            catListener = FirebaseRefs.categories().addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    List<Category> list = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snap.getChildren()) {
                        Category c = postSnapshot.getValue(Category.class);
                        if (c != null) {
                            list.add(c);
                        }
                    }
                    list.sort(Comparator.comparing(c ->
                            c.name == null ? "" : c.name.toLowerCase()
                    ));
                    adapter.submitCategories(list);
                }
                public void onCancelled(@NonNull DatabaseError e) {}
            });
        } else {
            // Displays items
            itemListener = FirebaseRefs.itemsByCategory(categoryId).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    List<Item> list = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snap.getChildren()) {
                        Item item = postSnapshot.getValue(Item.class);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                    list.sort((a,b) -> Long.compare(b.postedAt, a.postedAt));
                    adapter.submitItems(list);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }
    }
    @Override protected void onStop() {
        super.onStop();
        if (catListener!=null) FirebaseRefs.categories().removeEventListener(catListener);
        if (itemListener!=null) FirebaseRefs.itemsByCategory(categoryId).removeEventListener(itemListener);
    }

    // Adding, Editing, Deleting Categories
    // Shows the Add Categories dialog
    private void showAddCategory() {
        AddDialogFragment.newCategory(name -> {
            Category c = new Category();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            c.name = name;
            c.itemCount = 0;
            c.createdAt = System.currentTimeMillis();
            c.userid = user.getUid();
            addCategory(c);
        }).show(getSupportFragmentManager(), "addCat");
    }
    private void addCategory(Category c) {
        String key = FirebaseRefs.categories().push().getKey();

        if (key != null) {

            c.id = key;

            FirebaseRefs.categories().child(key).setValue(c)

                    .addOnSuccessListener(aVoid -> toast("Category added successfully"))

                    .addOnFailureListener(e -> toast("Failed to add category: " + e.getMessage()));

        }
    }
    private void editCategory(Category c) {
        if (c.itemCount > 0) {
            toast("Category must be empty");
            return;
        }
        EditDialogFragment.editCategory(c.name, (newName) -> {
            String trim = newName == null ? "" : newName.trim();
            long now = System.currentTimeMillis();

            HashMap<String,Object> updates = new HashMap<>();
            updates.put("name", trim);
            updates.put("updatedAt", now);

            FirebaseRefs.categories().child(c.id).updateChildren(updates);
            c.name = trim;
            c.updatedAt = now;
        }).show(getSupportFragmentManager(),"editCategory");

    }
    private void deleteCategory(Category c) {
        if (c.itemCount > 0) {

            toast("Cannot delete category with items. Delete all items first.");

            return;
        }
        new AlertDialog.Builder(this)

                .setTitle("Delete Category")

                .setMessage("Are you sure you want to delete " + c.name + "?")

                .setPositiveButton("Delete", (dialog, which) -> {

                    FirebaseRefs.categories().child(c.id).removeValue()

                            .addOnSuccessListener(aVoid -> toast("Category deleted successfully"))

                            .addOnFailureListener(e -> toast("Failed to delete category: " + e.getMessage()));

                })

                .setNegativeButton("Cancel", null)

                .show();
    }

    // Adding, Editing, Deleting Items
    // Shows the Add Item dialog
    private void showAddItem() {
        AddDialogFragment.newItem((itemName, desc, isFree, price) -> {
            Item item = new Item();
            item.name = itemName;
            item.description = desc;
            item.isFree = isFree;
            item.price = (price != null ? price : 0);
            item.categoryId = categoryId;
            item.postedAt = System.currentTimeMillis();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                item.postedByUID = user.getUid();
                String display = user.getDisplayName();
                if (display != null && !display.trim().isEmpty()) {
                    item.postedBy = display;
                } else {
                    item.postedBy = user.getEmail();
                }
            } else {
                item.postedByUID = null;
                item.postedBy = "Unknown";
            }
            addItem(item);
        }).show(getSupportFragmentManager(), "addItem");
    }
    private void addItem(Item item) {

        String key = FirebaseRefs.itemsByCategory(categoryId).push().getKey();

        if (key != null) {

            item.id = key;

            FirebaseRefs.itemsByCategory(categoryId).child(key).setValue(item)

                    .addOnSuccessListener(aVoid -> {

                        // Increment category item count

                        FirebaseRefs.categories().child(categoryId)

                                .child("itemCount")

                                .runTransaction(new com.google.firebase.database.Transaction.Handler() {

                                    @NonNull

                                    @Override

                                    public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData data) {

                                        Integer count = data.getValue(Integer.class);

                                        if (count == null) {

                                            data.setValue(1);

                                        } else {

                                            data.setValue(count + 1);

                                        }

                                        return com.google.firebase.database.Transaction.success(data);

                                    }



                                    @Override

                                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {

                                        if (committed) {

                                            toast("Item added successfully");

                                        }

                                    }

                                });

                    })

                    .addOnFailureListener(e -> toast("Failed to add item: " + e.getMessage()));

        }

    }
    private void editItem(Item item) {
        EditDialogFragment.editItem(item.name, item.description, item.isFree, item.price, (name, description, isFree, price) -> {
            HashMap<String,Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("description", description);
            updates.put("isFree", isFree);
            updates.put("price", isFree ? null : price);
            FirebaseRefs.itemsByCategory(item.categoryId).child(item.id).updateChildren(updates);
            FirebaseRefs.itemsByOwner(item.postedBy).child(item.id).updateChildren(updates);
        }).show(getSupportFragmentManager(),"editItem");
    }
    private void deleteItem(Item item) {

        new AlertDialog.Builder(this)

                .setTitle("Delete Item")

                .setMessage("Are you sure you want to delete " + item.name + "?")

                .setPositiveButton("Delete", (dialog, which) -> {

                    FirebaseRefs.itemsByCategory(categoryId).child(item.id).removeValue()

                            .addOnSuccessListener(aVoid -> {

                                // Decrement category item count

                                FirebaseRefs.categories().child(item.categoryId)

                                        .child("itemCount")

                                        .runTransaction(new com.google.firebase.database.Transaction.Handler() {

                                            @NonNull

                                            @Override

                                            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData data) {

                                                Integer count = data.getValue(Integer.class);

                                                if (count != null && count > 0) {

                                                    data.setValue(count - 1);

                                                }

                                                return com.google.firebase.database.Transaction.success(data);

                                            }



                                            @Override

                                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {

                                                if (committed) {

                                                    toast("Item deleted successfully");

                                                }

                                            }

                                        });

                            })

                            .addOnFailureListener(e -> toast("Failed to delete item: " + e.getMessage()));

                })

                .setNegativeButton("Cancel", null)

                .show();

    }
    // Buy item
    private void buyItem(Item item) {

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null

                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null

                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        String currentUserName = FirebaseAuth.getInstance().getCurrentUser() != null

                ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;

        if (currentUserId == null) {

            toast("You must be logged in to purchase items");

            return;

        }

        if (item.postedByUID == null) {
            toast("Seller UID missing");
            return;
        }

        String message = item.isFree

                ? "Do you want to accept this free item?"

                : "Do you want to purchase this item for $" + String.format("%.2f", item.price / 100.0) + "?";

        new AlertDialog.Builder(this)

                .setTitle(item.isFree ? "Accept Item" : "Purchase Item")

                .setMessage(message)

                .setPositiveButton(item.isFree ? "Accept" : "Buy", (dialog, which) -> {

                    String transactionKey = FirebaseRefs.completedByUser(currentUserId).push().getKey();

                    if (transactionKey != null) {

                        // Create transaction object

                        Transaction transaction = new Transaction();

                        transaction.id = transactionKey;

                        transaction.itemId = item.id;

                        transaction.itemName = item.name;

                        transaction.categoryID = item.categoryId;

                        transaction.buyerUserID = currentUserId;
                        transaction.buyerName = currentUserName;

                        transaction.sellerUserID = item.postedByUID;

                        transaction.sellerName = item.postedBy;

                        transaction.itemPrice = item.price;

                        transaction.itemIsFree = item.isFree;

                        transaction.status = "pending";

                        transaction.createdAt = System.currentTimeMillis();

                        transaction.buyerConfirmed = true;

                        transaction.sellerConfirmed = false;

                        // Save transaction to main transactions node

                        FirebaseRefs.transactions().child(transactionKey).setValue(transaction)

                                .addOnSuccessListener(aVoid -> {

                                    // Add to buyer's pending buys

                                    FirebaseRefs.pendingByUser(currentUserId).child(transactionKey).setValue(true);
                                    FirebaseRefs.pendingByUser(item.postedByUID).child(transactionKey).setValue(true);
                                    FirebaseRefs.itemsByCategory(item.categoryId)
                                            .child(item.id).removeValue()
                                            .addOnSuccessListener(aVoid2 -> {

                                                FirebaseRefs.itemsByCategory(categoryId).child(item.id).removeValue()

                                                        .addOnSuccessListener(aVoid3 -> {

                                                            // Decrement category item count

                                                            FirebaseRefs.categories().child(item.categoryId)

                                                                    .child("itemCount")

                                                                    .runTransaction(new com.google.firebase.database.Transaction.Handler() {

                                                                        @NonNull

                                                                        @Override

                                                                        public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData data) {

                                                                            Integer count = data.getValue(Integer.class);

                                                                            if (count != null && count > 0) {

                                                                                data.setValue(count - 1);

                                                                            }

                                                                            return com.google.firebase.database.Transaction.success(data);

                                                                        }



                                                                        @Override

                                                                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {

                                                                            if (committed) {

                                                                                toast(item.isFree ? "Item accepted! Seller has been notified."

                                                                                        : "Purchase initiated! Seller has been notified.");

                                                                            }

                                                                        }

                                                                    });

                                                        })

                                                        .addOnFailureListener(e -> toast("Failed to remove item: " + e.getMessage()));

                                            })

                                            .addOnFailureListener(e -> toast("Failed to update buyer list: " + e.getMessage()));

                                })

                                .addOnFailureListener(e -> toast("Transaction failed: " + e.getMessage()));

                    }

                })

                .setNegativeButton("Cancel", null)

                .show();

    }

    // Deals with opening categories
    public void onCategoryClicked(Category c) {
        // open items screen for this category
        Intent i = new Intent(this, CategoriesActivity.class);
        i.putExtra(EXTRA_MODE, MODE_ITEMS);
        i.putExtra(EXTRA_CATEGORY_ID, c.id);
        i.putExtra(EXTRA_CATEGORY_NAME, c.name);
        startActivity(i);
    }
    // Deals with editing and deleting categories
    public void onCategoryLong(Category c) {
        var user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = (user != null ? user.getUid() : null);
        boolean isOwner = UID != null && UID.equals(c.userid);
        if (isOwner) {
            String[] opts = {"Edit", "Delete "};
            new AlertDialog.Builder(this).setTitle(c.name).setItems(opts, (d, w) -> {
                if (w == 0) {
                    editCategory(c);
                } else {
                    deleteCategory(c);
                }
            }).show();
        }
    }

    // Deals with item interactions
    @Override
    public void onItemLong(Item item) {
        var user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = (user != null ? user.getUid() : null);
        boolean isOwner = UID != null && UID.equals(item.postedByUID);
        if (isOwner) {
            String[] opts = {"Edit", "Delete", "Buy"};
            new AlertDialog.Builder(this).setTitle(item.name).setItems(opts, (d, w) -> {
                if (w == 0) {
                    editItem(item);
                } else if (w == 1) {
                    deleteItem(item);
                } else {
                    buyItem(item);
                }
            }).show();
        } else {
            String[] opts = {"Buy"};
            new AlertDialog.Builder(this).setTitle(item.name).setItems(opts, (d, w) -> {
                buyItem(item);
            }).show();
        }
    }

    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }


    // Runs when options menu is created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_overflow, menu);
        return true;
    }

    // Runs when logout in options is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Sign out
        if (item.getItemId()==R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        // Home
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}