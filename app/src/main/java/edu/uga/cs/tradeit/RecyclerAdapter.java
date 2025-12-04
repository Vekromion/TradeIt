package edu.uga.cs.tradeit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.tradeit.models.Category;
import edu.uga.cs.tradeit.models.Item;

/**
 * Used for displaying categories and items inside said categories.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.VH> {

    // Callbacks allow for click and long press
    public interface OnRowAction {
        void onCategoryClicked(Category c);
        void onCategoryLong(Category c);
        void onItemLong(Item item);

    }

    private final Context context;
    private final int mode;
    private final OnRowAction OR;

    private List<Category> categories = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    private String currentCategoryName;
    public void setCurrentCategoryName(String name) {
        this.currentCategoryName = name;
    }

    public RecyclerAdapter(Context context, int mode) {
        this.context = context;
        this.mode = mode;
        this.OR = (OnRowAction) context;
    }

    // Updates categories
    public void submitCategories(List<Category> list) {
        categories = list;
        notifyDataSetChanged();
    }

    // Updates items
    public void submitItems(List<Item> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler, parent, false);
        return new VH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.VH holder, int position) {
        // Shows categories
        if (mode == CategoriesActivity.MODE_CATEGORIES) {
            Category c = categories.get(position);
            holder.title.setText(c.name);
            holder.line2.setText("Items: " + c.itemCount);
            holder.line3.setText("Posted on " + DateFormat.getDateTimeInstance().format(new Date(c.createdAt)));
            if (c.updatedAt > 0) {
                holder.line4.setText("Updated on " + DateFormat.getDateTimeInstance().format(new Date(c.updatedAt)));
            } else {
                holder.line4.setText("");
            }

            holder.itemView.setOnClickListener(v -> OR.onCategoryClicked(c));
            holder.itemView.setOnLongClickListener(v -> {
                OR.onCategoryLong(c);
                return true;
            });
        } else {
            // Shows items
            Item item = items.get(position);
            holder.title.setText(item.name);
            holder.line2.setText(item.isFree ? "FREE" : String.format(Locale.US, "$%.2f", item.price / 100.0));
            String categoryName = (currentCategoryName != null ? currentCategoryName : "Unknown");
            holder.line3.setText("Category: " + categoryName + " | " + item.description);
            String user = (item.postedBy != null ? item.postedBy : "unknown");
            holder.line4.setText("Posted by " + user + " on " + DateFormat.getDateTimeInstance().format(new Date(item.postedAt)));
            holder.itemView.setOnLongClickListener(v -> {
                OR.onItemLong(item);
                return true;
            });
        }
    }

    @Override public int getItemCount()
    {
        return mode==CategoriesActivity.MODE_CATEGORIES ? categories.size() : items.size();
    }
    // View holder for the rows
    static class VH extends RecyclerView.ViewHolder {
        TextView title, line2, line3, line4;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.textViewTitle);
            line2 = v.findViewById(R.id.textLine2);
            line3 = v.findViewById(R.id.textLine3);
            line4 = v.findViewById(R.id.textLine4);
        }
    }
}