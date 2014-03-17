package com.example.SpeakEasy;

import java.util.ArrayList;

public class QuotePost {
    private String quoteText, authorName, fbName, timestamp;
    private ArrayList<Integer> categories;

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getFbName() {
        return fbName;
    }

    public void setFbName(String fbName) {
        this.fbName = fbName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Integer> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Integer> categories) {
        this.categories = categories;
    }

   public QuotePost(String quoteText, String authorName, String fbName, String timestamp, ArrayList<Integer> categories) {

        this.quoteText = quoteText;
        this.authorName = authorName;
        this.fbName = fbName;
        this.timestamp = timestamp;
        this.categories = categories;

    }
}
