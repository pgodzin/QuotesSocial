package com.example.SpeakEasy;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

import java.util.ArrayList;

/**
 * This fragment is opened when a quote is submitted.
 * The user can choose 0 or more categories for the quote to go into,
 * and upon selection, the class saves the quote into the SimpleDB
 */
public class CategoryChooserFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Integer> mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        // Set the dialog title
        builder.title(R.string.pick_category)
                .positiveText("Submit")
                .negativeText("Cancel")
                .theme(Theme.LIGHT)
                .customView(R.layout.quote_edit, true)
                        // Specify the list array, the items to be selected by default (null for none),
                        // and the listener through which to receive callbacks when items are selected
                .items(R.array.categories)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        String cats = "";
                        for (Integer i : which) {
                            cats += i;

                        }
                        Toast.makeText(getActivity(), cats, Toast.LENGTH_SHORT).show();

                    }
                })/*
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
                        }
                )*/
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
                        String timestamp = SimpleDBUtils.encodeZeroPadding(System.currentTimeMillis() / 1000, 5);
                        String name = prefs.getString("name", "");
                        /*final QuotePost q = new QuotePost(quoteText.getText().toString(), author.getText().toString(),
                                name, timestamp, mSelectedItems);*/

                        //save the quote to the database in another thread
                        new Thread(new Runnable() {
                            public void run() {
                                //SimpleDB.addQuote(q);
                            }
                        }).start();

                        Toast.makeText(getActivity(), "Your quote has been added!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dismiss();
                    }
                });

        return builder.show();
    }
}