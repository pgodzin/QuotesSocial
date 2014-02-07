package com.example.SpeakEasy;

public class QuotePost {
    private String quoteText, authorName, fbName, timestamp;
    private String[] tags;

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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public QuotePost(String quoteText, String authorName, String fbName, String timestamp, String[] tags) {

        this.quoteText = quoteText;
        this.authorName = authorName;
        this.fbName = fbName;
        this.timestamp = timestamp;
        this.tags = tags;
    }
}
