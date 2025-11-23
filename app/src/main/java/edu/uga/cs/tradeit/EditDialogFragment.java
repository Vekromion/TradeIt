package edu.uga.cs.tradeit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

// This is a DialogFragment to handle edits to a Category or Item.
// The edits are: updates and deletions of existing Categories or Items.
public class EditDialogFragment extends DialogFragment {

    public interface onEditCategory {
        void save(String newName);
    }

    public interface onEditItem {
        void save(String name, String description, boolean isFree, Integer price);
    }

    private EditDialogFragment.onEditCategory editCat;
    private EditDialogFragment.onEditItem editItem;
    private boolean categoryMode = false;
    EditText name;
    EditText description;
    EditText price;
    CheckBox free;
    private String newName;
    private String newDesc;
    private boolean newFree;
    private Integer newPrice;

    public EditDialogFragment() {
        // Required empty public constructor
    }

    public static EditDialogFragment editCategory(String name, EditDialogFragment.onEditCategory editCat) {
        EditDialogFragment frag = new EditDialogFragment();
        frag.categoryMode = true;
        frag.newName = name;
        frag.editCat = editCat;
        return frag;
    }

    public static EditDialogFragment editItem(String name, String description, Boolean isFree, Integer price, EditDialogFragment.onEditItem editItem) {
        EditDialogFragment frag = new EditDialogFragment();
        frag.categoryMode = false;
        frag.newName = name;
        frag.newDesc = description;
        frag.newFree = isFree;
        frag.newPrice = price;
        frag.editItem = editItem;
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create the AlertDialog view
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.fragment_add_dialog,
                getActivity().findViewById(R.id.root));

        // get the view objects in the AlertDialog
        name = layout.findViewById(R.id.editTextText4);
        description = layout.findViewById(R.id.editTextTextMultiLine);
        price = layout.findViewById(R.id.editTextNumberDecimal);
        free = layout.findViewById(R.id.checkBox);

        // Pre fill the fields
        name.setText(newName);
        // Changes visibility of description depending on if category or items are shown
        if (categoryMode) {
            description.setVisibility(View.GONE);
            price.setVisibility(View.GONE);
            free.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);
            free.setVisibility(View.VISIBLE);

            description.setText(newDesc);

            free.setChecked(newFree);
            if (newPrice != null && !newFree) {
                price.setText(String.valueOf(newPrice));
            } else {
                price.setText(null);
            }
            price.setEnabled(!free.isChecked());
        }

        if (free != null) {
            free.setOnCheckedChangeListener((button, checked) -> {
                price.setEnabled(!checked);

                if (checked) {
                    price.setText(null);
                }
            });
        }

        // create a new AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle);
        // Set its view (inflated above).
        builder.setView(layout);

        // Set the title of the AlertDialog
        builder.setTitle(categoryMode ? "Update Category" : "Update Item");
        // Provide the negative button listener
        builder.setNegativeButton("Cancel", null);
        // Provide the positive button listener
        builder.setPositiveButton("Save", new EditDialogFragment.SaveListener());
        /**
        builder.setNeutralButton("Delete", (d, which) -> {
            if (categoryMode) {
                if (editCat != null) editCat.delete();
            } else {
                if (editItem != null) editItem.delete();
            }
            dismiss();
        });**/

        // Create the AlertDialog and show it
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.teal_700
            );

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accent);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(accent);
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(accent);
        });
        return dialog;
    }

    private class SaveListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String nameStr = name.getText().toString();
            String descStr = description.getText().toString();
            if (TextUtils.isEmpty(nameStr)) {
                name.setError("Name required");
                return;
            }
            // If category is being updated
            if (categoryMode) {
                if (editCat != null) {
                    editCat.save(nameStr);
                }
            } else {
                // If an item is being updated
                boolean isFree = free.isChecked();
                Integer priceItem = null;

                if (!isFree) {
                    String pr = price.getText().toString().trim();
                    if (TextUtils.isEmpty(pr)) {
                        isFree = true;
                        priceItem = 0;
                    }
                }
                if (editItem != null) {
                    editItem.save(nameStr, descStr, isFree, priceItem);
                }
            }
            dismiss();
        }
    }
}
