package com.example.SpeakEasy.categoryActivities;

import android.os.Bundle;
import android.widget.TextView;
import com.example.SpeakEasy.MainPageListFragment;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;

public class FunnyFeedFragment extends MainPageListFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        TextView tv = (TextView) getActivity().findViewById(R.id.newsFeed);
        tv.setText(R.string.funnyfeed);

        itemNames = SimpleDB.getFeedItemNamesByCategory("advice");

        adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
        setListAdapter(adapter);

    }
}