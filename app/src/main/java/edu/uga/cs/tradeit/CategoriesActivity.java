package edu.uga.cs.tradeit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.uga.cs.tradeit.data.FirebaseRefs;
import edu.uga.cs.tradeit.models.Category;
import edu.uga.cs.tradeit.models.Item;

public class CategoriesActivity extends AppCompatActivity implements RecyclerAdapter.OnRowAction {

    public static final String EXTRA_MODE = "mode" ;
    public static final String EXTRA_CATEGORY_ID = "categoryId";
    public static final String EXTRA_CATEGORY_NAME = "categoryName";
    public static final int MODE_CATEGORIES = 0;
    public static final int MODE_ITEMS = 1;

    private static final boolean DEMO_MODE = true;

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
        rcView = findViewById(R.id.rcView);
        rcView.setLayoutManager(new LinearLayoutManager(this));

        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_CATEGORIES);
        categoryId = getIntent().getStringExtra(EXTRA_CATEGORY_ID);
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);

        setTitle(mode == MODE_CATEGORIES ? "Categories" : ("Items in " + categoryName));

        adapter = new RecyclerAdapter(this, mode);
        rcView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> {
            if (mode==MODE_CATEGORIES) showAddCategory();
            else showAddItem();
        });
        // Temporary Fake Data
        if (DEMO_MODE) {
            if (mode == MODE_CATEGORIES) {
                // --- Test Category ---
                List<Category> fake = new ArrayList<>();
                Category c = new Category();
                Category d = new Category();
                c.id = "testCat";
                c.name = "Test Category";
                c.itemCount = 1;
                d.id = "testCat2";
                d.name = "U Test Category";
                d.itemCount = 0;
                fake.add(c);
                fake.add(d);
                adapter.submitCategories(fake);
            } else {
                // --- Test Item in this category ---
                List<Item> fakeItems = new ArrayList<>();

                if ("testCat".equals(categoryId)) {
                    Item it = new Item();
                    it.id = "testItem1";
                    it.categoryId = categoryId;     // "testCat"
                    it.name = "Test Item";
                    it.description = "This is a test item";
                    it.isFree = false;
                    it.price = 999;                  // $9.99
                    it.postedAt = System.currentTimeMillis();

                    fakeItems.add(it);
                }
                adapter.submitItems(fakeItems);
            }
        }
    }
    // Connects Page to Firebase
    @Override protected void onStart() {
        super.onStart();
        // Demo Mode avoids starting up firebase listeners
        if (DEMO_MODE) {
            return;
        }
        if (mode == MODE_CATEGORIES) {
            catListener = FirebaseRefs.categories().addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    List<Category> list = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snap.getChildren()) {
                        Category c = postSnapshot.getValue(Category.class);
                        if (c != null) {
                            list.add(c);
                        }
                    }
                    // Story 4a
                    list.sort(Comparator.comparing(c -> c.name.toLowerCase()));
                    adapter.submitCategories(list);
                }
                public void onCancelled(@NonNull DatabaseError e) {}
            });
        } else {
            itemListener = FirebaseRefs.itemsByCategory(categoryId).addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    List<Item> list = new ArrayList<>();
                    for (DataSnapshot postSnapshot: snap.getChildren()) {
                        Item item = postSnapshot.getValue(Item.class);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                    // Story 11A
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

    }
    private void addCategory(Category c) {

    }
    private void editCategory(Category c) {

    }
    private void deleteCategory(Category c) {

    }

    // Adding, Editing, Deleting Items
    // Shows the Add Item dialog
    private void showAddItem() {

    }
    private void editItem(Item item) {

    }
    private void deleteItem(Item item) {

    }
    // User Story 12
    private void buyItem(Item item) {

    }

    public void onCategoryClicked(Category c) {
        // open items screen for this category
        Intent i = new Intent(this, CategoriesActivity.class);
        i.putExtra(EXTRA_MODE, MODE_ITEMS);
        i.putExtra(EXTRA_CATEGORY_ID, c.id);
        i.putExtra(EXTRA_CATEGORY_NAME, c.name);
        startActivity(i);
    }
    public void onCategoryLong(Category c) {
        String[] opts = {"Edit (only if empty)", "Delete (only if empty)"};
        new AlertDialog.Builder(this).setTitle(c.name).setItems(opts,(d, w)->{
            if (w==0) editCategory(c); else deleteCategory(c);
        }).show();
    }

    @Override
    public void onItemLong(Item item) {

    }

    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_LONG).show(); }

}