package com.example.SpeakEasy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;

import java.util.HashMap;
import java.util.List;

/**
 * ListFragment that displays user's own posts
 */
public class HomePageListFragment extends SherlockListFragment {
    protected List<String> itemNames;
    protected static MySimpleArrayAdapter adapter;
    private UiLifecycleHelper uiHelper;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle("Home Feed");
        final String name = this.getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", "");
        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getMyQuotesItemNames(name);
                adapter = new MySimpleArrayAdapter(inflater.getContext(), itemNames);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setListAdapter(adapter);

                    }
                });
            }
        }).start();
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    public static void addQuoteToAdapter(String itemName) {
        adapter.add(itemName);
        adapter.notifyDataSetChanged();
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

    /**
     * When fbShare icon is pressed, create on OpenGraphAction that says you posted a new quote
     */
    private void shareToFB(String quoteText) {
        // TODO: consider adding badges for sharing
        if (FacebookDialog.canPresentOpenGraphActionDialog(getActivity().getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            OpenGraphObject quote = OpenGraphObject.Factory.createForPost
                    (OpenGraphObject.class, "speakeasydevfest:post", "I posted a new quote!",
                            "http://i.imgur.com/ec9p33P.jpg", null, "\"" + quoteText + "\"");
            OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
            action.setProperty("quote", quote);
            action.setType("speakeasydevfest:post");
            FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(getActivity(), action, "quote")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(getActivity(), "Facebook not available", Toast.LENGTH_SHORT).show();
        }
    }

    protected static class ViewHolder {
        TextView quoteText;
        TextView quoteAuthor;
        TextView fbShare;
        TextView fav;
        TextView homeNumFavs;
        ImageView heart;
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
                convertView = inflater.inflate(R.layout.home_item_view, parent, false);

                // set up the ViewHolder
                viewHolder = new ViewHolder();
                viewHolder.fbShare = (TextView) convertView.findViewById(R.id.FBshare);
                viewHolder.fav = (TextView) convertView.findViewById(R.id.favorite);
                viewHolder.homeNumFavs = (TextView) convertView.findViewById(R.id.homeNumFavorites);
                viewHolder.heart = (ImageView) convertView.findViewById(R.id.homeHeart);
                convertView.setTag(viewHolder);
            } else {
                // we've just avoided calling findViewById() on resource every time
                // just use the viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            viewHolder.timestamp = attrMap.get("timestamp");
            viewHolder.quoteText = (TextView) convertView.findViewById(R.id.itemText);
            viewHolder.quoteAuthor = (TextView) convertView.findViewById(R.id.itemAuthor);
            viewHolder.quoteAuthor.setText(attrMap.get("author"));
            viewHolder.quoteText.setText(attrMap.get("quoteText"));
            final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
            final String yourName = prefs.getString("name", "");
            final String nameSpaceless = yourName.replace(" ", "");
            viewHolder.postID = nameSpaceless + viewHolder.timestamp;

            final Resources res = convertView.getResources();
            final int[] numFavs = new int[1];
            final boolean[] isFav = new boolean[1];
            new Thread(new Runnable() {
                public void run() {
                    numFavs[0] = SimpleDB.favCount(viewHolder.postID);
                    isFav[0] = SimpleDB.isFavoritedByUser(viewHolder.postID, nameSpaceless);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (numFavs[0] == 0 && viewHolder.homeNumFavs.getVisibility() == View.VISIBLE) {
                                viewHolder.homeNumFavs.setVisibility(View.INVISIBLE);
                            } else if (numFavs[0] == 1) {
                                viewHolder.homeNumFavs.setText("1 Favorite");
                                viewHolder.homeNumFavs.setVisibility(View.VISIBLE);
                            } else if (viewHolder.homeNumFavs.getVisibility() == View.VISIBLE) {
                                viewHolder.homeNumFavs.setText(numFavs[0] + " Favorites");
                            }

                            if (isFav[0]) {
                                viewHolder.heart.setImageResource(R.drawable.redheart);
                            } else if (!isFav[0]) {
                                viewHolder.heart.setImageResource(R.drawable.greyheart);
                            }
                        }
                    });
                }
            }).start();

            viewHolder.fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Stop trying to like your own post!", Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(viewHolder.quoteText.getText().toString());
                }
            });

            return convertView;
        }
    }
}