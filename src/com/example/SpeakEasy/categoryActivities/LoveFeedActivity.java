package com.example.SpeakEasy.categoryActivities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.SpeakEasy.MainPage;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;

public class LoveFeedActivity extends MainPage {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) findViewById(R.id.newsFeed);
        tv.setText(R.string.lovefeed);

        itemNames = SimpleDB.getFeedItemNamesByCategory("love");

        adapter = new MySimpleArrayAdapter(this, itemNames);
        setListAdapter(adapter);

    }
}