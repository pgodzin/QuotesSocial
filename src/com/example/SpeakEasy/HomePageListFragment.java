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

public class HomePageListFragment extends SherlockListFragment {
    protected List<String> itemNames;
    protected static MySimpleArrayAdapter adapter;
    private UiLifecycleHelper uiHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        itemNames = SimpleDB.getMyQuotesItemNames(this.getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", ""));
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle("Main Feed");
        adapter = new MySimpleArrayAdapter(inflater.getContext(), itemNames);
        setListAdapter(adapter);
        return inflater.inflate(R.layout.list_fragment, container, false);
    }

    public static void addQuoteToAdapter(String itemName){
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
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.home_item_view, parent, false);
            final TextView quoteText = (TextView) rowView.findViewById(R.id.itemText);
            TextView quoteAuthor = (TextView) rowView.findViewById(R.id.itemAuthor);
            ImageView fbShare = (ImageView) rowView.findViewById(R.id.fbshare);

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            quoteAuthor.setText(attrMap.get("author"));
            quoteText.setText(attrMap.get("quoteText"));

            int numFavs = SimpleDB.favCount(attrMap.get("fbName").replace(" ", "") + attrMap.get("timestamp"));

            Button homeFav = (Button) rowView.findViewById(R.id.homeFavorite);
            homeFav.setText("" + numFavs);

            if (Integer.parseInt(homeFav.getText().toString()) == 0) {
                homeFav.setTextColor(getResources().getColor(R.color.grayheartText));
            } else homeFav.setTextColor(getResources().getColor(android.R.color.black));

            homeFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Stop trying to like your own post", Toast.LENGTH_SHORT).show();
                }
            });

            fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(quoteText.getText().toString());
                }
            });

            return rowView;
        }
    }
}