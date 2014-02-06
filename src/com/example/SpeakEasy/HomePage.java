package com.example.SpeakEasy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;

public class HomePage extends SherlockActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        Button b = (Button) findViewById(R.id.submit);
        final EditText quote = (EditText) findViewById(R.id.quoteText);
        final EditText author = (EditText) findViewById(R.id.quoteAuthor);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, quote.getText() + " - " + author.getText(), Toast.LENGTH_LONG).show();
                //Simple DB integration
            }
        });

    }
}