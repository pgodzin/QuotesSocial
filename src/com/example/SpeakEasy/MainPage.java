package com.example.SpeakEasy;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.example.SpeakEasy.categoryFragments.*;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import it.neokree.materialnavigationdrawer.MaterialAccount;
import it.neokree.materialnavigationdrawer.MaterialAccountListener;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.MaterialSection;

import java.io.InputStream;
import java.net.URL;

public class MainPage extends MaterialNavigationDrawer implements MaterialAccountListener {

    public static AmazonClientManager clientManager = null;
    protected UiLifecycleHelper uiHelper;

    MaterialSection main, myQuotes, following, popular, advice, funny, inspirational, love, movie, settings;
    MaterialAccount account;

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                Bitmap profilePic;
                URL imgUrl = new URL(getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("profile_url", ""));
                InputStream in = (InputStream) imgUrl.getContent();
                profilePic = BitmapFactory.decodeStream(in);
                account.setPhoto(profilePic);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyAccountDataChanged();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void init(Bundle savedInstanceState) {

        clientManager = new AmazonClientManager(getSharedPreferences("speakeasySDB", Context.MODE_PRIVATE));
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        getToolbar().setTitle("All Quotes");
        this.disableLearningPattern();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

            switchToSearchFragment(query);
        }

        // TODO: fix
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final String name = getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", "");
        account = new MaterialAccount(name, "", new ColorDrawable(Color.parseColor("#9e9e9e")),
                getResources().getDrawable(R.drawable.navigation_bar_background_blue));
        main = this.newSection("All Quotes", this.getResources().getDrawable(R.drawable.ic_action_edit), new MainPageListFragment()).setNotifications(10);
        myQuotes = this.newSection("My Quotes", this.getResources().getDrawable(R.drawable.ic_action_edit), new MyQuotesFeedFragment())
                .setSectionColor(Color.parseColor("#2196f3"), Color.parseColor("#1565c0")).setNotifications(150);
        following = this.newSection("Following", this.getResources().getDrawable(android.R.drawable.ic_input_add), new FollowingFeedFragment()).setNotifications(10);
        popular = this.newSection("Most Popular", this.getResources().getDrawable(android.R.drawable.star_big_off), new PopularFeedFragment()).setNotifications(10);
        advice = this.newSection("Advice Quotes", this.getResources().getDrawable(android.R.drawable.ic_menu_help), new AdviceFeedFragment()).setNotifications(10);
        funny = this.newSection("Funny Quotes", this.getResources().getDrawable(android.R.drawable.ic_menu_help), new FunnyFeedFragment()).setNotifications(10);
        inspirational = this.newSection("Inspirational Quotes", this.getResources().getDrawable(android.R.drawable.ic_menu_help), new InspirationalFeedFragment()).setNotifications(10);
        love = this.newSection("Love Quotes", this.getResources().getDrawable(R.drawable.greyheart), new LoveFeedFragment()).setNotifications(10);
        movie = this.newSection("Movie Quotes", this.getResources().getDrawable(android.R.drawable.ic_menu_help), new MovieFeedFragment()).setNotifications(10);
        settings = this.newSection("Settings", this.getResources().getDrawable(android.R.drawable.ic_menu_manage));

        // add your sections to the drawer
        this.addSection(main);
        this.addSection(myQuotes);
        this.addSection(following);
        this.addDivisor();
        this.addSection(popular);
        this.addSection(advice);
        this.addSection(funny);
        this.addSection(inspirational);
        this.addSection(love);
        this.addSection(movie);
        this.addBottomSection(settings);

        this.setBackPattern(MaterialNavigationDrawer.BACKPATTERN_BACK_TO_FIRST);

        this.addAccount(account);
        t.start();
    }

    @Override
    public void onAccountOpening(MaterialAccount account) {
        // open profile activity
    }

    @Override
    public void onChangeAccount(MaterialAccount newAccount) {
        // when another account is selected
    }

    /**
     * Places the searchFragment into the MainPage activity, which looks up by the query based on which fragment the
     * search came from. If from the main feed, all quotes get searched.
     *
     * @param query query term to search author, poster, and quoteText by
     */
    private void switchToSearchFragment(String query) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        String fragmentName = getToolbar().getTitle().toString().toLowerCase().split(" ")[0];
        Fragment fragment = newInstance(query, fragmentName);
        ft.replace(it.neokree.materialnavigationdrawer.R.id.frame_container, fragment).commit();
        //getSupportFragmentManager().beginTransaction().add(it.neokree.materialnavigationdrawer.R.id.frame_container,
        //newInstance(query, getToolbar().getTitle().toString().toLowerCase().split(" ")[0])).commit();
    }

    public static SearchListFragment newInstance(String query, String fragmentName) {
        SearchListFragment myFragment = new SearchListFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        args.putString("fragmentName", fragmentName);
        myFragment.setArguments(args);

        return myFragment;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        menu.findItem(R.id.search).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_item, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));

        // Assumes current activity is the searchable activity
        SearchManager searchManager = (SearchManager) getApplicationContext().getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

    public void replaceFragment(Fragment fragment, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null).commit();
    }
}