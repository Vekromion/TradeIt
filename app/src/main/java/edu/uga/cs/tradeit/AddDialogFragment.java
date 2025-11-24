package edu.uga.cs.tradeit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

// A DialogFragment class to handle Category and Item additions from the Cateogries activity
// It uses a DialogFragment to allow the input of a new category or item.
public class AddDialogFragment extends DialogFragment {

    public interface onNewCategory {
        void create(String name);
    }
    public interface onNewItem {
        void create(String name, String description, boolean isFree, Integer price);
    }

    private onNewCategory newCat;
    private onNewItem newItem;
    private boolean categoryMode = false;
    EditText name;
    EditText description;
    EditText price;
    CheckBox free;

    public AddDialogFragment () {
        // Required empty public constructor
    }

    public static AddDialogFragment newCategory(onNewCategory newCat) {
        AddDialogFragment frag = new AddDialogFragment();
        frag.categoryMode = true;
        frag.newCat = newCat;
        return frag;
    }
    public static AddDialogFragment newItem(onNewItem newItem) {
        AddDialogFragment frag = new AddDialogFragment();
        frag.categoryMode = false;
        frag.newItem = newItem;
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

        // Changes visibility of description depending on if category or items are shown
        if (categoryMode) {
            description.setVisibility(View.GONE);
            price.setVisibility(View.GONE);
            free.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            price.setVisibility(View.VISIBLE);
            free.setVisibility(View.VISIBLE);
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
        builder.setTitle(categoryMode ? "New Category" : "New Item");
        // Provide the negative button listener
        builder.setNegativeButton( "Cancel", null );
        // Provide the positive button listener
        builder.setPositiveButton("Save", new SaveListener());

        // Create the AlertDialog and show it
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.teal_700
            );
            final Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            final Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            if (btnPositive != null) {
                btnPositive.setTextColor(accent);
                btnPositive.setOnClickListener(v ->
                        new SaveListener().onClick(dialog, AlertDialog.BUTTON_POSITIVE)
                );
            }
            if (btnNegative != null) {
                btnNegative.setTextColor(accent);
            }

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
            // If category is being added
            if (categoryMode) {
                if (newCat != null) {
                    newCat.create(nameStr);
                }
            } else {
                // If an item is being added
                boolean isFree = free.isChecked();
                Integer priceItem = null;

                if (!isFree) {
                    String pr = price.getText().toString().trim();
                    if (TextUtils.isEmpty(pr)) {
                        isFree = true;
                        priceItem = 0;
                    }
                }

                if (newItem != null) {
                    newItem.create(nameStr, descStr, isFree, priceItem);
                }
            }
            dismiss();
        }
    }
}