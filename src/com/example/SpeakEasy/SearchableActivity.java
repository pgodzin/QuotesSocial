package com.example.SpeakEasy;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListActivity;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

import java.util.HashMap;
import java.util.List;

public class SearchableActivity extends SherlockListActivity {

    protected MySimpleArrayAdapter adapter;
    protected List<String> itemNames;
    private UiLifecycleHelper uiHelper;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchlayout);
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }


        });
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void search(String query) {
        itemNames = SimpleDB.searchByQuery(query.replace(" ", ""));
        adapter = new MySimpleArrayAdapter(this, itemNames);
        setListAdapter(adapter);
    }

    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private List<String> quoteItemNames;

        public MySimpleArrayAdapter(Context context, List<String> values) {
            super(context, R.layout.main_item_view, values);
            this.context = context;
            this.quoteItemNames = values;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.main_item_view, parent, false);
            final TextView fbName = (TextView) rowView.findViewById(R.id.mainFBName);
            final TextView quoteText = (TextView) rowView.findViewById(R.id.mainItemText);
            TextView quoteAuthor = (TextView) rowView.findViewById(R.id.mainItemAuthor);
            ImageView fbShare = (ImageView) rowView.findViewById(R.id.mainFBshare);

            final Button fav = (Button) rowView.findViewById(R.id.mainFavorite);

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            fbName.setText(attrMap.get("fbName"));
            quoteAuthor.setText(attrMap.get("author"));
            quoteText.setText(attrMap.get("quoteText"));
            fav.setText(attrMap.get("favorites"));

            final SharedPreferences prefs = getSharedPreferences("fbInfo", Context.MODE_PRIVATE);

            final String nameSpaceless = prefs.getString("name", "").replace(" ", "");
            if (!prefs.getBoolean(nameSpaceless + "FavoritesCreated", false)) {
                SimpleDB.createDomain(nameSpaceless + "Favorites");
                prefs.edit().putBoolean(nameSpaceless + "FavoritesCreated", true).commit();
            }

            final String timestamp = attrMap.get("timestamp");
            final String postID = fbName.getText().toString().replace(" ", "") + timestamp;

            SelectRequest selectRequest = new SelectRequest("select * from " + nameSpaceless + "Favorites" + " where postID = '" + postID + "'").withConsistentRead(true);
            final List<Item> items = HomePage.clientManager.sdb().select(selectRequest).getItems();
            if (items.size() == 1)
                fav.setBackground(rowView.getResources().getDrawable(R.drawable.redheart));
            else fav.setBackground(rowView.getResources().getDrawable(R.drawable.greyheart));

            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int numFavs = Integer.parseInt(fav.getText().toString());
                    if (items.size() == 1) {
                        SimpleDB.deleteItem(nameSpaceless + "Favorites", postID);
                        HashMap<String, String> newFavs = new HashMap<String, String>();
                        newFavs.put("favorites", "" + (numFavs - 1));
                        //fav.setText("" + numFavs);
                        SimpleDB.updateAttributesForItem("Quotes", postID, newFavs);
                        adapter.notifyDataSetChanged();
                    } else if (items.size() == 0) {
                        SimpleDB.createItem("Quotes", postID);
                        HashMap<String, String> newFavs = new HashMap<String, String>();
                        newFavs.put("favorites", "" + (numFavs + 1));
                        //fav.setText("" + numFavs);
                        SimpleDB.updateAttributesForItem("Quotes", postID, newFavs);
                        SimpleDB.addToFavoriteTable(postID, nameSpaceless);
                        adapter.notifyDataSetChanged();

                    }

                }
            });

            fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FBUtil.shareToFB(getApplicationContext(), SearchableActivity.this, quoteText.getText().toString(), uiHelper);
                }
            });
            return rowView;

        }

    }
}