package com.example.SpeakEasy.categoryActivities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import com.example.SpeakEasy.AmazonClientManager;
import com.example.SpeakEasy.MainPage;
import com.example.SpeakEasy.R;
import com.example.SpeakEasy.SimpleDB;
import com.facebook.UiLifecycleHelper;

public class FunnyFeedActivity extends MainPage {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        TextView tv = (TextView) findViewById(R.id.newsFeed);
        tv.setText(R.string.funnyfeed);

        itemNames = SimpleDB.getFeedItemNamesByCategory("advice");

        adapter = new MySimpleArrayAdapter(this, itemNames);
        setListAdapter(adapter);

    }
}