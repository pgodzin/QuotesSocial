package com.example.SpeakEasy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.SpeakEasy.tvmclient.Response;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;

public class HomePage extends SherlockFragmentActivity {
    public static AmazonClientManager clientManager = null;
    private UiLifecycleHelper uiHelper;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clientManager = new AmazonClientManager(getSharedPreferences("speakeasySDB", Context.MODE_PRIVATE));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        //TODO: fix
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
            case android.R.id.home:
                startActivity(new Intent(HomePage.this, MainPage.class));
                return true;
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
                } else {
                    b.setVisibility(View.VISIBLE);
                    quote.setVisibility(View.VISIBLE);
                    author.setVisibility(View.VISIBLE);
                }

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selectCategories();

                    }
                });
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectCategories() {
        DialogFragment newFragment = new CategoryChooserFragment();
        newFragment.show(getSupportFragmentManager(), "categories");
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