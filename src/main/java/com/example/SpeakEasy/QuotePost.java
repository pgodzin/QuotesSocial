package main.java.com.example.SpeakEasy;

/**
 * Class representing a single Quote
 */
public class QuotePost {
    private String quoteText;
    private String authorName;
    private String fbName;
    private String userId;
    private String timestamp;
    private Integer[] categories;

    public QuotePost(String quoteText, String authorName, String fbName, String userId, String timestamp,
                     Integer[] categories) {
        this.quoteText = quoteText;
        this.authorName = authorName;
        this.fbName = fbName;
        this.userId = userId;
        this.timestamp = timestamp;
        this.categories = categories;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getFbName() {
        return fbName;
    }

    public String getUserId() {
        return userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer[] getCategories() {
        return categories;
    }
}
