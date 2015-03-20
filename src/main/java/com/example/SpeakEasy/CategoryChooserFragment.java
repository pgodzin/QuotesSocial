package main.java.com.example.SpeakEasy;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

/**
 * This fragment is opened when a quote is submitted.
 * The user can choose 0 or more categories for the quote to go into,
 * and upon selection, the class saves the quote into the SimpleDB
 */
public class CategoryChooserFragment extends DialogFragment {

    protected MaterialNavigationDrawer mActivity;

    public static boolean isNetworkAvailable(final Activity context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean available = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        if (!available) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "No internet service currently available.", Toast.LENGTH_LONG).show();
                }
            });
        }
        return available;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());

        builder.title(R.string.pick_category)
                .positiveText("Submit")
                .negativeText("Cancel")
                .theme(Theme.LIGHT)
                .customView(R.layout.quote_edit, true)
                .items(R.array.categories)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
                        String name = prefs.getString("name", "");
                        String userId = prefs.getString("userId", "");

                        String timestamp = SimpleDBUtils.encodeZeroPadding(System.currentTimeMillis() / 1000, 5);
                        EditText quoteText = (EditText) dialog.getCustomView().findViewById(R.id.quote_edit_text);
                        EditText author = (EditText) dialog.getCustomView().findViewById(R.id.author_edit_text);
                        final QuotePost q = new QuotePost(quoteText.getText().toString(), author.getText().toString(),
                                name, userId, timestamp, which);

                        //save the quote to the database in another thread
                        if (isNetworkAvailable(mActivity)) {
                            new Thread(new Runnable() {
                                public void run() {
                                    SimpleDB.addQuote(q);
                                }
                            }).start();
                        }
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Toast.makeText(getActivity(), "Your quote has been added!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dismiss();
                    }
                });

        return builder.show();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MaterialNavigationDrawer) activity;
    }
}