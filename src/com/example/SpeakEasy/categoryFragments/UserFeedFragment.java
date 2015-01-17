package com.example.SpeakEasy.categoryFragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;
import com.facebook.UiLifecycleHelper;

public class UserFeedFragment extends MainPageListFragment {
    private String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(mActivity, null);
        uiHelper.onCreate(savedInstanceState);
        username = getArguments().getString("username");
        getActivity().setTitle(getFragmentTitle());
        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getUserItemNames(username);
                adapter = new MainPageListFragment.MySimpleArrayAdapter(mActivity, itemNames);
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
                        itemNames = SimpleDB.getUserItemNames(username);
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
        return username;
    }
}