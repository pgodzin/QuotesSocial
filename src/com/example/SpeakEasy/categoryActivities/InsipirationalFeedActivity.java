package com.example.SpeakEasy.categoryActivities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.SpeakEasy.MainPage;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;

public class InsipirationalFeedActivity extends MainPage {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) findViewById(R.id.newsFeed);
        tv.setText(R.string.inspirationalfeed);

        itemNames = SimpleDB.getFeedItemNamesByCategory("inspirational");

        adapter = new MySimpleArrayAdapter(this, itemNames);
        setListAdapter(adapter);

    }
}