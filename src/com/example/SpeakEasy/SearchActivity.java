package com.example.SpeakEasy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

public class SearchActivity extends SherlockFragmentActivity {

    public static AmazonClientManager clientManager = null;
    protected UiLifecycleHelper uiHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientManager = new AmazonClientManager(getSharedPreferences("speakeasySDB", Context.MODE_PRIVATE));
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.searchpage);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        String fragmentName = intent.getStringExtra("fragmentName");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.search_view, newInstance(query, fragmentName)).commit();

        // TODO: fix
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static SearchListFragment newInstance(String query, String fragmentName) {
        SearchListFragment myFragment = new SearchListFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        args.putString("fragmentName", fragmentName);
        myFragment.setArguments(args);

        return myFragment;
    }

    // TODO: Maybe pop up full screen quote with comment feed
    /*    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(MainPage.this, "Selected " + position, Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
}