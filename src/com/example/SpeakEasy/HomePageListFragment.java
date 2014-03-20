package com.example.SpeakEasy;

import android.content.Context;
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

/**
 * ListFragment that displays user's own posts
 */
public class HomePageListFragment extends SherlockListFragment {
    protected List<String> itemNames;
    protected static MySimpleArrayAdapter adapter;
    private UiLifecycleHelper uiHelper;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get user's quotes
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
     *
     * @param quoteText
     */
    private void shareToFB(String quoteText) {

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
        ImageView fbShare;
        Button homeFav;
        String postID;
        String timestamp;
    }

    /**
     * Custom Array Adapter - different from MainPage one as it does not
     * show name of the poster as they are all your posts.
     */
    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private List<String> quoteItemNames;

        public MySimpleArrayAdapter(Context context, List<String> values) {
            super(context, R.layout.home_item_view, values);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {

                // inflate the layout
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.home_item_view, parent, false);

                // set up the ViewHolder
                viewHolder = new ViewHolder();

                viewHolder.fbShare = (ImageView) convertView.findViewById(R.id.fbshare);

                viewHolder.homeFav = (Button) convertView.findViewById(R.id.homeFavorite);
                convertView.setTag(viewHolder);

            } else {
                // we've just avoided calling findViewById() on resource every time
                // just use the viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));

            viewHolder.quoteAuthor = (TextView) convertView.findViewById(R.id.itemAuthor);
            viewHolder.quoteText = (TextView) convertView.findViewById(R.id.itemText);

            viewHolder.quoteAuthor.setText(attrMap.get("author"));
            viewHolder.quoteText.setText(attrMap.get("quoteText"));
            viewHolder.timestamp = attrMap.get("timestamp");
            viewHolder.postID = attrMap.get("fbName").replace(" ", "") + viewHolder.timestamp;

            new Thread(new Runnable() {
                public void run() {
                    final int numFavs = SimpleDB.favCount(viewHolder.postID);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.homeFav.setText("" + numFavs);
                            //don't show number of favorites if 0
                            if (Integer.parseInt(viewHolder.homeFav.getText().toString()) == 0) {
                                viewHolder.homeFav.setTextColor(getResources().getColor(R.color.grayheartText));
                            } else viewHolder.homeFav.setTextColor(getResources().getColor(android.R.color.black));
                        }
                    });

                }
            }).start();

            viewHolder.homeFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Stop trying to like your own post", Toast.LENGTH_SHORT).show();
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