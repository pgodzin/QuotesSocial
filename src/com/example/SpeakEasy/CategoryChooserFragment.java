package com.example.SpeakEasy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

import java.util.ArrayList;

public class CategoryChooserFragment extends SherlockDialogFragment {
    //protected HomePageListFragment.MySimpleArrayAdapter adapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.pick_category)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.categories, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog

                        final Button b = (Button) getActivity().findViewById(R.id.submit);
                        final EditText quote = (EditText) getActivity().findViewById(R.id.quoteText);
                        final EditText author = (EditText) getActivity().findViewById(R.id.quoteAuthor);

                        final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);

                        String timestamp = SimpleDBUtils.encodeZeroPadding(System.currentTimeMillis() / 1000, 5);
                        String name = prefs.getString("name", "");
                        QuotePost q = new QuotePost(quote.getText().toString(), author.getText().toString(),
                                name, timestamp, new String[0], 0);
                        if (!prefs.getBoolean("quotesDomainCreated", false)) {
                            SimpleDB.createDomain("Quotes");
                            prefs.edit().putBoolean("quotesDomainCreated", true).commit();
                        }
                        SimpleDB.addQuote(q);

                        HomePageListFragment.addQuoteToAdapter(name + "" + timestamp);

                        quote.setText("");
                        author.setText("");

                        b.setVisibility(View.GONE);
                        quote.setVisibility(View.GONE);
                        author.setVisibility(View.GONE);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}