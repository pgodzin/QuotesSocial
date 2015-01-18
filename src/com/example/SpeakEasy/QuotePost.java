package com.example.SpeakEasy;

import java.util.ArrayList;

/**
 * Class representing a single Quote
 */
public class QuotePost {
    private String quoteText, authorName, fbName, timestamp;
    private Integer[] categories;

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getFbName() {
        return fbName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer[] getCategories() {
        return categories;
    }

    public QuotePost(String quoteText, String authorName, String fbName, String timestamp,
                     Integer[] categories) {
        this.quoteText = quoteText;
        this.authorName = authorName;
        this.fbName = fbName;
        this.timestamp = timestamp;
        this.categories = categories;
    }
}
