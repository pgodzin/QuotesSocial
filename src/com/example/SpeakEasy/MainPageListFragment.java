package com.example.SpeakEasy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListFragment;
import com.example.SpeakEasy.categoryFragments.UserFeedFragment;
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
        getActivity().setTitle("Main Feed");
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
        TextView fbShare;
        ImageView follow;
        TextView mainFav;
        TextView numFavs;
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
                convertView = inflater.inflate(R.layout.main_item_view, parent, false);

                // set up the ViewHolder
                viewHolder = new ViewHolder();
                viewHolder.fbShare = (TextView) convertView.findViewById(R.id.mainFBshare);
                viewHolder.follow = (ImageView) convertView.findViewById(R.id.mainFollow);
                viewHolder.mainFav = (TextView) convertView.findViewById(R.id.mainFavorite);
                viewHolder.numFavs = (TextView) convertView.findViewById(R.id.numFavorites);
                viewHolder.heart = (ImageView) convertView.findViewById(R.id.heart);
                convertView.setTag(viewHolder);
            } else {
                // we've just avoided calling findViewById() on resource every time
                // just use the viewHolder
                viewHolder = (ViewHolder) convertView.getTag();
            }

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            viewHolder.fbName = (TextView) convertView.findViewById(R.id.mainFBName);
            viewHolder.fbName.setText(attrMap.get("fbName"));
            viewHolder.timestamp = attrMap.get("timestamp");
            viewHolder.postID = viewHolder.fbName.getText().toString().replace(" ", "") + viewHolder.timestamp;
            viewHolder.quoteText = (TextView) convertView.findViewById(R.id.mainItemText);
            viewHolder.quoteAuthor = (TextView) convertView.findViewById(R.id.mainItemAuthor);
            viewHolder.quoteAuthor.setText(attrMap.get("author"));
            viewHolder.quoteText.setText(attrMap.get("quoteText"));
            final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
            final String yourName = prefs.getString("name", "");
            final String nameSpaceless = yourName.replace(" ", "");

            if (viewHolder.fbName.getText().toString().equals(yourName)) {
                viewHolder.follow.setVisibility(View.GONE);
            }

            final Resources res = convertView.getResources();
            final String posterName = viewHolder.fbName.getText().toString();
            final int[] numFavs = new int[1];
            final boolean[] isFav = new boolean[1];
            new Thread(new Runnable() {
                public void run() {
                    numFavs[0] = SimpleDB.favCount(viewHolder.postID);
                    isFav[0] = SimpleDB.isFavoritedByUser(viewHolder.postID, nameSpaceless);
                    final boolean isFollowed = SimpleDB.isFollowedByUser(posterName, yourName);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (numFavs[0] == 0 && viewHolder.numFavs.getVisibility() == View.VISIBLE) {
                                viewHolder.numFavs.setVisibility(View.INVISIBLE);
                            } else if (numFavs[0] == 1) {
                                viewHolder.numFavs.setText("1 Favorite");
                                viewHolder.numFavs.setVisibility(View.VISIBLE);
                            } else if (viewHolder.numFavs.getVisibility() == View.VISIBLE) {
                                viewHolder.numFavs.setText(numFavs[0] + " Favorites");
                            }

                            if (isFav[0]) {
                                viewHolder.heart.setImageResource(R.drawable.redheart);
                            } else if (!isFav[0]) {
                                viewHolder.heart.setImageResource(R.drawable.greyheart);
                            }

                            if (isFollowed || posterName.equals(yourName)) {
                                viewHolder.follow.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder.follow.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();

            viewHolder.mainFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final HashMap<String, String> newFavAttr = new HashMap<String, String>();
                    if (posterName.equals(yourName)) {
                        Toast.makeText(getActivity(), "Stop trying to like your own post!", Toast.LENGTH_SHORT).show();
                    } else if (isFav[0]) {
                        new Thread(new Runnable() {
                            public void run() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.heart.setImageResource(R.drawable.greyheart);
                                        if (numFavs[0] == 1) {
                                            viewHolder.numFavs.setVisibility(View.INVISIBLE);
                                        } else if (numFavs[0] == 2) {
                                            viewHolder.numFavs.setText("1 Favorite");
                                        } else {
                                            viewHolder.numFavs.setText(numFavs[0] - 1 + " Favorites");
                                        }
                                    }
                                });
                                SimpleDB.deleteItem("Favorites", viewHolder.postID + "_likedBy_" + nameSpaceless);
                                newFavAttr.put("favorites", "" + (numFavs[0] - 1));
                                SimpleDB.updateAttributesForItem("Quotes", viewHolder.postID, newFavAttr);
                                //adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.heart.setImageResource(R.drawable.redheart);
                                        if (numFavs[0] == 0) {
                                            viewHolder.numFavs.setVisibility(View.VISIBLE);
                                            viewHolder.numFavs.setText("1 Favorite");
                                        } else {
                                            viewHolder.numFavs.setText(numFavs[0] + 1 + " Favorites");
                                        }
                                    }
                                });
                                SimpleDB.addToFavoriteTable(viewHolder.postID, nameSpaceless);
                                newFavAttr.put("favorites", "" + (numFavs[0] + 1));
                                SimpleDB.updateAttributesForItem("Quotes", viewHolder.postID, newFavAttr);
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

            viewHolder.fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToFB(getActivity(), viewHolder.quoteText.getText().toString(), uiHelper);
                }
            });

            viewHolder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        public void run() {
                            SimpleDB.addToFollowingTable(posterName, yourName);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }).start();
                }
            });

            viewHolder.fbName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment = new UserFeedFragment(posterName);
                    fragmentTransaction.replace(R.id.content_frame, fragment);
                    fragmentTransaction.addToBackStack(null).commit();
                }
            });
            return convertView;
        }
    }
}