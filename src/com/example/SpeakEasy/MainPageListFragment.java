package com.example.SpeakEasy;

import android.app.Activity;
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
    protected UiLifecycleHelper uiHelper;

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setListAdapter(adapter);

                    }
                });
            }
        }).start();

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

    public static void shareToFB(Activity activity, String quoteText, UiLifecycleHelper uiHelper) {

        if (FacebookDialog.canPresentOpenGraphActionDialog(activity.getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            OpenGraphObject quote = OpenGraphObject.Factory.createForPost
                    (OpenGraphObject.class, "speakeasydevfest:post", "I loved this quote!",
                            "http://i.imgur.com/ec9p33P.jpg", null, "\"" + quoteText + "\"");
            OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
            action.setProperty("quote", quote);
            action.setType("speakeasydevfest:love");

            FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(activity, action, "quote")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(activity, "Facebook not available", Toast.LENGTH_SHORT).show();
        }
    }

    protected static class ViewHolder {
        TextView fbName;
        TextView quoteText;
        TextView quoteAuthor;
        ImageView fbShare;
        ImageView follow;
        Button mainFav;
        String postID;
        String timestamp;
    }

    protected class MySimpleArrayAdapter extends ArrayAdapter<String> {
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


            final ViewHolder viewHolder;
            if (convertView == null) {

                // inflate the layout
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.main_item_view, parent, false);

                // well set up the ViewHolder
                viewHolder = new ViewHolder();
                viewHolder.fbName = (TextView) convertView.findViewById(R.id.mainFBName);
                viewHolder.quoteText = (TextView) convertView.findViewById(R.id.mainItemText);
                viewHolder.quoteAuthor = (TextView) convertView.findViewById(R.id.mainItemAuthor);
                viewHolder.fbShare = (ImageView) convertView.findViewById(R.id.mainFBshare);
                viewHolder.follow = (ImageView) convertView.findViewById(R.id.mainFollow);

                HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
                viewHolder.fbName.setText(attrMap.get("fbName"));
                viewHolder.quoteAuthor.setText(attrMap.get("author"));
                viewHolder.quoteText.setText(attrMap.get("quoteText"));

                viewHolder.timestamp = attrMap.get("timestamp");
                viewHolder.postID = viewHolder.fbName.getText().toString().replace(" ", "") + viewHolder.timestamp;

                viewHolder.mainFav = (Button) convertView.findViewById(R.id.mainFavorite);
                convertView.setTag(viewHolder);

            } else {
                // we've just avoided calling findViewById() on resource every time
                // just use the viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
            final String yourName = prefs.getString("name", "");
            final String nameSpaceless = yourName.replace(" ", "");

            if(viewHolder.fbName.getText().toString().equals(yourName))
                viewHolder.follow.setVisibility(View.GONE);

            final int numFavs = SimpleDB.favCount(viewHolder.postID);
            viewHolder.mainFav.setText("" + numFavs);// store the holder with the view.

            if (Integer.parseInt(viewHolder.mainFav.getText().toString()) == 0) {
                viewHolder.mainFav.setTextColor(getResources().getColor(R.color.grayheartText));
            } else viewHolder.mainFav.setTextColor(getResources().getColor(android.R.color.black));

            final boolean isFav = SimpleDB.isFavoritedByUser(viewHolder.postID, nameSpaceless);

            if (isFav)
                viewHolder.mainFav.setBackground(convertView.getResources().getDrawable(R.drawable.redheart));
            else viewHolder.mainFav.setBackground(convertView.getResources().getDrawable(R.drawable.greyheart));

            viewHolder.mainFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String,String> newFavAttr = new HashMap<String, String>();
                    if (isFav) {
                        SimpleDB.deleteItem("Favorites", viewHolder.postID + "_likedBy_" + nameSpaceless);
                        newFavAttr.put("favorites", "" + (numFavs - 1));
                        SimpleDB.updateAttributesForItem("Quotes", viewHolder.postID, newFavAttr);
                        adapter.notifyDataSetChanged();
                    } else {
                        SimpleDB.addToFavoriteTable(viewHolder.postID, nameSpaceless);
                        newFavAttr.put("favorites", "" + (numFavs + 1));
                        SimpleDB.updateAttributesForItem("Quotes", viewHolder.postID, newFavAttr);
                        adapter.notifyDataSetChanged();
                    }

                }
            });

            viewHolder.fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(getActivity(), viewHolder.quoteText.getText().toString(), uiHelper);
                }
            });

            viewHolder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return convertView;
        }


    }
}