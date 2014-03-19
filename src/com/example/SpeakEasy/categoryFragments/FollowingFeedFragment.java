package com.example.SpeakEasy.categoryFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;
import com.facebook.UiLifecycleHelper;

/**
 * ListFragment that displays all the quotes posted with an 'advice' category tag
 */
public class FollowingFeedFragment extends MainPageListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle("Followed Quotes");
        final SharedPreferences prefs = getActivity().getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
        final String name = prefs.getString("name", "");

        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getFollowingFeedItemNames(name);
                adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
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
}
