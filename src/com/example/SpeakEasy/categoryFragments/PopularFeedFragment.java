package com.example.SpeakEasy.categoryFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;
import com.facebook.UiLifecycleHelper;

import java.util.HashMap;
import java.util.List;

/**
 * ListFragment that displays all the quotes posted in order of number of favorites
 * */
public class PopularFeedFragment extends MainPageListFragment {
    MyPopularArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle("Popular Quotes");
        final String name = this.getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", "");
        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getPopularFeedItemNames(name);
                adapter = new MyPopularArrayAdapter(getActivity(), name, itemNames);
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

    protected class MyPopularArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private List<String> quoteItemNames;
        private String name;

        public MyPopularArrayAdapter(Context context, String name, List<String> values) {
            super(context, R.layout.main_item_view, values);
            this.context = context;
            this.name = name;
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

            // inflate the layout
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.main_item_view, parent, false);

            // well set up the ViewHolder
            TextView fbName = (TextView) rowView.findViewById(R.id.mainFBName);
            final TextView quoteText = (TextView) rowView.findViewById(R.id.mainItemText);
            TextView quoteAuthor = (TextView) rowView.findViewById(R.id.mainItemAuthor);
            ImageView fbShare = (ImageView) rowView.findViewById(R.id.mainFBshare);
            ImageView follow = (ImageView) rowView.findViewById(R.id.mainFollow);

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            fbName.setText(attrMap.get("fbName"));
            quoteAuthor.setText(attrMap.get("author"));
            quoteText.setText(attrMap.get("quoteText"));

            String timestamp = attrMap.get("timestamp");
            final String postID = fbName.getText().toString().replace(" ", "") + timestamp;

            Button mainFav = (Button) rowView.findViewById(R.id.mainFavorite);

            final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
            final String yourName = prefs.getString("name", "");
            final String nameSpaceless = yourName.replace(" ", "");

            if (fbName.getText().toString().equals(yourName))
                follow.setVisibility(View.GONE);

            final int numFavs = SimpleDB.favCount(postID);
            mainFav.setText("" + numFavs);// store the holder with the view.

            if (Integer.parseInt(mainFav.getText().toString()) == 0) {
                mainFav.setTextColor(getResources().getColor(R.color.grayheartText));
            } else mainFav.setTextColor(getResources().getColor(android.R.color.black));

            final boolean isFav = SimpleDB.isFavoritedByUser(postID, nameSpaceless);

            if (isFav)
                mainFav.setBackground(rowView.getResources().getDrawable(R.drawable.redheart));
            else mainFav.setBackground(rowView.getResources().getDrawable(R.drawable.greyheart));

            //TODO: doesn't always redraw when improper ordering
            mainFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final HashMap<String, String> newFavAttr = new HashMap<String, String>();
                    if (isFav) {
                        new Thread(new Runnable() {
                            public void run() {
                                quoteItemNames = SimpleDB.getPopularFeedItemNames(name);
                                SimpleDB.deleteItem("Favorites", postID + "_likedBy_" + nameSpaceless);
                                newFavAttr.put("favorites", "" + (numFavs - 1));
                                SimpleDB.updateAttributesForItem("Quotes", postID, newFavAttr);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();

                    } else {
                        new Thread(new Runnable() {
                            public void run() {
                                quoteItemNames = SimpleDB.getPopularFeedItemNames(name);
                                SimpleDB.addToFavoriteTable(postID, nameSpaceless);
                                newFavAttr.put("favorites", "" + (numFavs + 1));
                                SimpleDB.updateAttributesForItem("Quotes", postID, newFavAttr);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();
                    }

                }
            });

            fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(getActivity(), quoteText.getText().toString(), uiHelper);
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
