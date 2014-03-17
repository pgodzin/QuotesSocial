package com.example.SpeakEasy.categoryActivities;

import android.os.Bundle;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.SimpleDB;

public class AdviceFeedFragment extends MainPageListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemNames = SimpleDB.getFeedItemNamesByCategory("advice");

        adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
        setListAdapter(adapter);

    }
}