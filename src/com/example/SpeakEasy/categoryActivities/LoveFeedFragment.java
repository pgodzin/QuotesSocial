package com.example.SpeakEasy.categoryActivities;

import android.os.Bundle;
import android.widget.TextView;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;

public class LoveFeedFragment extends MainPageListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) getActivity().findViewById(R.id.newsFeed);
        tv.setText(R.string.lovefeed);

        itemNames = SimpleDB.getFeedItemNamesByCategory("love");

        adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
        setListAdapter(adapter);

    }
}