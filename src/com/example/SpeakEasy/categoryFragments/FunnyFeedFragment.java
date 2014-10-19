package com.example.SpeakEasy.categoryFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;
import com.facebook.UiLifecycleHelper;

/**
 * ListFragment that displays all the quotes posted with a 'funny' category tag
 */
public class FunnyFeedFragment extends MainPageListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle(getFragmentTitle());
        new Thread(new Runnable() {
            public void run() {
                itemNames = SimpleDB.getFeedItemNamesByCategory("funny");
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

    @Override
    public String getFragmentTitle() {
        return "Funny Quotes";
    }
}