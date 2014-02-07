package com.example.SpeakEasy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainPage extends SherlockActivity {
    protected ListView quoteList;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        SharedPreferences prefs = getSharedPreferences("fbInfo", Context.MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.search_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                  Toast.makeText(MainPage.this, "Searched", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.home:
                Intent i = new Intent(MainPage.this, HomePage.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}