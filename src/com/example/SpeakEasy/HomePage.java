package com.example.SpeakEasy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.example.SpeakEasy.tvmclient.Response;

public class HomePage extends SherlockActivity {
    public static AmazonClientManager clientManager = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        Button b = (Button) findViewById(R.id.submit);
        final EditText quote = (EditText) findViewById(R.id.quoteText);
        final EditText author = (EditText) findViewById(R.id.quoteAuthor);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientManager = new AmazonClientManager(getSharedPreferences(
                        "speakeasySDB", Context.MODE_PRIVATE));
                if (HomePage.clientManager.hasCredentials()) {

                } else {
                    displayCredentialsIssueAndExit();
                }

                Toast.makeText(HomePage.this, quote.getText() + " - " + author.getText(), Toast.LENGTH_LONG).show();
                quote.setText("");
                author.setText("");
                //Simple DB integration
            }
        });

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