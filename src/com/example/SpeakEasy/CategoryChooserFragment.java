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
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

import java.util.ArrayList;

/**
 * This fragment is opened when a quote is submitted in the HomePage.
 * The user can choose 0 or more categories for the quote to go into,
 * and upon selection, the class saves the quote into the SimpleDB
 */
public class CategoryChooserFragment extends SherlockDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList mSelectedItems = new ArrayList<Integer>();  // Where we track the selected items
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
                        }
                )
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
                        final QuotePost q = new QuotePost(quote.getText().toString(), author.getText().toString(),
                                name, timestamp, mSelectedItems);

                        //save the quote to the database in another thread
                        new Thread(new Runnable() {
                            public void run() {
                                SimpleDB.addQuote(q);
                            }
                        }).start();

                        //add itemName to main activity's ListAdapter
                        String postID = name + "" + timestamp;
                        //HomePageListFragment.addQuoteToAdapter(postID);
                        // TODO: How can this quote be available right after?
                        Toast.makeText(getActivity(), "Your quote has been added!", Toast.LENGTH_SHORT).show();
                        quote.setText("");
                        author.setText("");

                        //hide the UI to enter a quote
                        b.setVisibility(View.GONE);
                        quote.setVisibility(View.GONE);
                        author.setVisibility(View.GONE);
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