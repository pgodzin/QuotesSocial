package com.example.SpeakEasy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.example.SpeakEasy.tvmclient.Response;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;

import java.util.HashMap;
import java.util.List;

public class HomePage extends SherlockListActivity {
    public static AmazonClientManager clientManager = null;
    protected List<String> itemNames;
    protected MySimpleArrayAdapter adapter;
    static HomePage activity = null;
    private UiLifecycleHelper uiHelper;


    public void onCreate(Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);
        activity = this;

        //TODO: fix
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        clientManager = new AmazonClientManager(getSharedPreferences("speakeasySDB", Context.MODE_PRIVATE));

        itemNames = SimpleDB.getMyQuotesItemNames(getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("name", ""));

        adapter = new MySimpleArrayAdapter(this, itemNames);
        setListAdapter(adapter);


    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(HomePage.this, "Selected " + position, Toast.LENGTH_SHORT).show();
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
                } else {
                    b.setVisibility(View.VISIBLE);
                    quote.setVisibility(View.VISIBLE);
                    author.setVisibility(View.VISIBLE);
                }

                final SharedPreferences prefs = getSharedPreferences("fbInfo", Context.MODE_PRIVATE);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (HomePage.clientManager.hasCredentials()) {

                        } else {
                            displayCredentialsIssueAndExit();
                        }
                        String timestamp = SimpleDBUtils.encodeZeroPadding(System.currentTimeMillis() / 1000, 5);
                        String name = prefs.getString("name", "");
                        QuotePost q = new QuotePost(quote.getText().toString(), author.getText().toString(),
                                name, timestamp, new String[0]);
                        if (!prefs.getBoolean("quotesDomainCreated", false)) {
                            SimpleDB.createDomain("Quotes");
                            prefs.edit().putBoolean("quotesDomainCreated", true);
                        }
                        SimpleDB.addQuote(q);
                        adapter.add(name + "" + timestamp);
                        adapter.notifyDataSetChanged();

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


    private void shareToFB(String quoteText) {

        if (FacebookDialog.canPresentOpenGraphActionDialog(this.getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            OpenGraphObject quote = OpenGraphObject.Factory.createForPost
                    (OpenGraphObject.class, "speakeasydevfest:quote", "I posted a new quote!",
                            "http://i.imgur.com/ec9p33P.jpg", null, quoteText);
            OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
            action.setProperty("quote", quote);
            action.setType("speakeasydevfest:quote");

            FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(HomePage.this, action, "quote")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(HomePage.this, "Facebook not available", Toast.LENGTH_SHORT).show();
        }
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

    public class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private List<String> quoteItemNames;

        public MySimpleArrayAdapter(Context context, List<String> values) {
            super(context, R.layout.item_view, values);
            this.context = context;
            this.quoteItemNames = values;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public void add(String object) {
            final Object mLock = new Object();
            synchronized (mLock) {
                if (quoteItemNames == null) {
                    quoteItemNames.add(object);
                } else {
                    quoteItemNames.add(0, object);
                }
            }
        }

//        public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
//
//        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_view, parent, false);
            TextView fbName = (TextView) rowView.findViewById(R.id.fbName);
            final TextView quoteText = (TextView) rowView.findViewById(R.id.itemText);
            TextView quoteAuthor = (TextView) rowView.findViewById(R.id.itemAuthor);
            ImageView fbShare = (ImageView) rowView.findViewById(R.id.fbshare);
            ImageView follow = (ImageView) rowView.findViewById(R.id.follow);
            ImageView fav = (ImageView) rowView.findViewById(R.id.favorite);

            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            fbShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  shareToFB(quoteText.getText().toString());
                }
            });

            follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", quoteItemNames.get(position));
            fbName.setText(attrMap.get("fbName"));
            quoteAuthor.setText(attrMap.get("author"));
            quoteText.setText(attrMap.get("quoteText"));

            return rowView;
        }
    }
}