package com.example.SpeakEasy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.example.SpeakEasy.tvmclient.Response;

public class HomePage extends SherlockActivity {
    public static AmazonClientManager clientManager = null;
    protected ListView quoteList;
    private QuotePost[] myQuotes;
    //private QuoteAdapter qAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        quoteList = (ListView) findViewById(R.id.listView);
        quoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Click ListItem Number " + position, Toast.LENGTH_LONG)
                        .show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.search_with_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                Toast.makeText(HomePage.this, "Searched", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.edit:
                final Button b = (Button) findViewById(R.id.submit);
                final EditText quote = (EditText) findViewById(R.id.quoteText);
                final EditText author = (EditText) findViewById(R.id.quoteAuthor);
                if (b.getVisibility() == View.VISIBLE) {
                    b.setVisibility(View.GONE);
                    quote.setVisibility(View.GONE);
                    author.setVisibility(View.GONE);
                }
                else{
                    b.setVisibility(View.VISIBLE);
                    quote.setVisibility(View.VISIBLE);
                    author.setVisibility(View.VISIBLE);
                }

                final SharedPreferences prefs = getSharedPreferences("fbInfo", Context.MODE_PRIVATE);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clientManager = new AmazonClientManager(getSharedPreferences(
                                "speakeasySDB", Context.MODE_PRIVATE));
                        if (HomePage.clientManager.hasCredentials()) {

                        } else {
                            displayCredentialsIssueAndExit();
                        }
                        String timestamp = SimpleDBUtils.encodeZeroPadding(System.currentTimeMillis() / 1000, 5);
                        QuotePost q = new QuotePost(quote.getText().toString(), author.getText().toString(),
                                prefs.getString("name", ""), timestamp, new String[0]);
                        SimpleDB.addQuote(q);
                        quote.setText("");
                        author.setText("");

                        b.setVisibility(View.GONE);
                        quote.setVisibility(View.GONE);
                        author.setVisibility(View.GONE);
                    }
                });
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle("Credential Problem!");
        confirm.setMessage("AWS Credentials not configured correctly");
        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HomePage.this.finish();
            }
        });
        confirm.show().show();
    }

    protected void displayErrorAndExit(Response response) {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        if (response == null) {
            confirm.setTitle("Error Code Unkown");
            confirm.setMessage("Please review the README file.");
        } else {
            confirm.setTitle("Error Code [" + response.getResponseCode() + "]");
            confirm.setMessage(response.getResponseMessage()
                    + "\nPlease review the README file.");
        }

        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HomePage.this.finish();
            }
        });
        confirm.show().show();
    }

    private class ValidateCredentialsTask extends
            AsyncTask<Class<?>, Void, com.example.SpeakEasy.tvmclient.Response> {

        Class<?> cls;

        protected com.example.SpeakEasy.tvmclient.Response doInBackground(Class<?>... classes) {

            cls = classes[0];
            return HomePage.clientManager.validateCredentials();
        }

        protected void onPostExecute(com.example.SpeakEasy.tvmclient.Response response) {
            if (response != null && response.requestWasSuccessful()) {
                startActivity(new Intent(HomePage.this, cls));
            } else {
                HomePage.this.displayErrorAndExit(response);
            }
        }

    }
}