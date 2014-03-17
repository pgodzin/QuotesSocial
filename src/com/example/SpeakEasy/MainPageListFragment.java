package com.example.SpeakEasy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;

import java.util.HashMap;
import java.util.List;

public class MainPageListFragment extends SherlockListFragment {
    protected List<String> itemNames;
    protected static MySimpleArrayAdapter adapter;
    private UiLifecycleHelper uiHelper;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final String name = this.getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", "");
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        //TODO: why is this necessary?
        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getFeedItemNames(name);
                adapter = new MySimpleArrayAdapter(inflater.getContext(), itemNames);

            }
        }).start();
        setListAdapter(adapter);

        return inflater.inflate(R.layout.main_listfragment, container, false);
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

    private void shareToFB(String quoteText) {

        if (FacebookDialog.canPresentOpenGraphActionDialog(getActivity().getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            OpenGraphObject quote = OpenGraphObject.Factory.createForPost
                    (OpenGraphObject.class, "speakeasydevfest:post", "I loved this quote!",
                            "http://i.imgur.com/ec9p33P.jpg", null, "\"" + quoteText + "\"");
            OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
            action.setProperty("quote", quote);
            action.setType("speakeasydevfest:love");

            FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(getActivity(), action, "quote")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(getActivity(), "Facebook not available", Toast.LENGTH_SHORT).show();
        }
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
        public void add(String object) {
            final Object mLock = new Object();
            synchronized (mLock) {
                if (quoteItemNames == null) {
                    quoteItemNames.add(object);
                } else {
                    quoteItemNames.add(0, object);
                }
            }
        }

        @Override
        public void remove(String object) {
            super.remove(object);
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
            ImageView follow = (ImageView) rowView.findViewById(R.id.mainFollow);

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            fbName.setText(attrMap.get("fbName"));
            quoteAuthor.setText(attrMap.get("author"));
            quoteText.setText(attrMap.get("quoteText"));

            final String timestamp = attrMap.get("timestamp");
            final String postID = fbName.getText().toString().replace(" ", "") + timestamp;

            int numFavs = SimpleDB.favCount(postID);

            Button mainFav = (Button) rowView.findViewById(R.id.mainFavorite);
            mainFav.setText("" + numFavs);

            if (Integer.parseInt(mainFav.getText().toString()) == 0) {
                mainFav.setTextColor(getResources().getColor(R.color.grayheartText));
            } else mainFav.setTextColor(getResources().getColor(android.R.color.black));

            final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);

            final String nameSpaceless = prefs.getString("name", "").replace(" ", "");
            if (!prefs.getBoolean(nameSpaceless + "FavoritesCreated", false)) {
                SimpleDB.createDomain(nameSpaceless + "Favorites");
                prefs.edit().putBoolean(nameSpaceless + "FavoritesCreated", true).commit();
            }

            final boolean isFav = SimpleDB.isFavoritedByUser(postID, nameSpaceless);

            if (isFav)
                mainFav.setBackground(rowView.getResources().getDrawable(R.drawable.redheart));
            else mainFav.setBackground(rowView.getResources().getDrawable(R.drawable.greyheart));

            mainFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFav) {
                        SimpleDB.deleteItem("Favorites", postID + "_likedBy_" + nameSpaceless);
                        adapter.notifyDataSetChanged();
                    } else {
                        SimpleDB.addToFavoriteTable(postID, nameSpaceless);
                        adapter.notifyDataSetChanged();
                    }

                }
            });

            fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(quoteText.getText().toString());
                }
            });

            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return rowView;
        }
    }
}