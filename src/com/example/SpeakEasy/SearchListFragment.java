package com.example.SpeakEasy;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.facebook.UiLifecycleHelper;

public class SearchListFragment extends MainPageListFragment {
    private String query;
    private String fragmentName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(mActivity, null);
        uiHelper.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            public void run() {
                if (!getArguments().isEmpty()) {
                    query = getArguments().getString("query");
                    itemNames = SimpleDB.getItemNamesBySearchQuery(query);
                    adapter = new MySimpleArrayAdapter(mActivity, itemNames);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (itemNames.size() == 0) {
                                String cat = "";
                                if (!fragmentName.equals("all")) {
                                    cat = "in " + fragmentName + " quotes.";
                                }
                                Toast.makeText(mActivity.getApplicationContext(),
                                        "No quotes available by this search term" + cat, Toast.LENGTH_LONG).show();
                            }
                            mActivity.setTitle(query);
                            setListAdapter(adapter);

                        }
                    });
                }
            }
        }).start();

        return inflater.inflate(R.layout.main_listfragment, container, false);
    }

    /**
     * Called when the listView is pulled down for the data to refresh
     */
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        query = getArguments().getString("query");
                        itemNames = SimpleDB.getItemNamesBySearchQuery(query);
                        adapter = new MySimpleArrayAdapter(mActivity, itemNames);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setListAdapter(adapter);
                                swipeLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        }, 0);
    }

    @Override
    public String getFragmentTitle() {
        return "Advice Quotes";
    }
}