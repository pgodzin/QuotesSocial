package main.java.com.example.SpeakEasy;

import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Util class for database transactions
 */
public class SimpleDB {
    public static final String QUOTES = "Quotes";
    private static String nextToken = null;
    private static int prevNumDomains = 0;

    public static AmazonSimpleDBClient getInstance() {
        return MainPage.simpleDBClient;
    }

    public static List<String> getDomainNames() {
        return getInstance().listDomains().getDomainNames();
    }

    public static List<String> getDomainNames(int numDomains) {
        prevNumDomains = numDomains;
        return getDomainNames(numDomains, null);
    }

    /**
     * @param numDomains
     * @param nextToken
     * @return list of domain names
     */
    private static List<String> getDomainNames(int numDomains, String nextToken) {
        ListDomainsRequest req = new ListDomainsRequest();
        req.setMaxNumberOfDomains(numDomains);
        if (nextToken != null)
            req.setNextToken(nextToken);
        ListDomainsResult result = getInstance().listDomains(req);
        List<String> domains = result.getDomainNames();
        SimpleDB.nextToken = result.getNextToken();
        return domains;
    }

    public static List<String> getMoreDomainNames() {
        if (nextToken == null) {
            return new ArrayList<String>();
        } else {
            return getDomainNames(prevNumDomains, nextToken);
        }
    }

    public static void createDomain(String domainName) {
        getInstance().createDomain(new CreateDomainRequest(domainName));
    }

    public static void deleteDomain(String domainName) {
        getInstance().deleteDomain(new DeleteDomainRequest(domainName));
    }

    public static void createItem(String domainName, String itemName) {
        List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(1);
        attributes.add(new ReplaceableAttribute().withName("Name").withValue("Value"));
        getInstance().putAttributes(new PutAttributesRequest(domainName, itemName, attributes));
    }

    public static void createAttributeForItem(String domainName, String itemName, String attributeName, String attributeValue) {
        List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(1);
        attributes.add(new ReplaceableAttribute().withName(attributeName).withValue(attributeValue).withReplace(true));
        getInstance().putAttributes(new PutAttributesRequest(domainName, itemName, attributes));
    }

    public static String[] getItemNamesForDomain(String domainName) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from `" + domainName + "`").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        String[] itemNames = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemNames[i] = items.get(i).getName();
        }
        return itemNames;
    }

    /**
     * A map of attributes for a specific item in a table
     *
     * @param domainName table name
     * @param itemName   itemName for the item
     * @return map of attribute name to value
     */
    public static HashMap<String, String> getAttributesForItem(String domainName, String itemName) {
        GetAttributesRequest getRequest = new GetAttributesRequest(domainName, itemName).withConsistentRead(true);
        GetAttributesResult getResult = getInstance().getAttributes(getRequest);

        HashMap<String, String> attributes = new HashMap<String, String>(30);
        for (Object attribute : getResult.getAttributes()) {
            String name = ((Attribute) attribute).getName();
            String value = ((Attribute) attribute).getValue();
            attributes.put(name, value);
        }
        return attributes;
    }

    /**
     * Update attribute values
     *
     * @param domainName table name
     * @param itemName   itemName for the item
     * @param attributes map of attribute name and value to replace
     */
    public static void updateAttributesForItem(String domainName, String itemName, HashMap<String, String> attributes) {
        List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>(attributes.size());
        for (String attributeName : attributes.keySet()) {
            replaceableAttributes.add(new ReplaceableAttribute().withName(attributeName).withValue(attributes.get(attributeName)).withReplace(true));
        }
        getInstance().putAttributes(new PutAttributesRequest(domainName, itemName, replaceableAttributes));
    }

    /**
     * Delete an item
     *
     * @param domainName table name
     * @param itemName   itemName for the item
     */
    public static void deleteItem(String domainName, String itemName) {
        getInstance().deleteAttributes(new DeleteAttributesRequest(domainName, itemName));
    }

    public static void deleteItemAttribute(String domainName, String itemName, String attributeName) {
        getInstance().deleteAttributes(new DeleteAttributesRequest(domainName, itemName)
                .withAttributes(new Attribute[]{new Attribute().withName(attributeName)}));
    }

    /**
     * Adds a quote and the userID of the user who favorited it to the Favorites table
     *
     * @param postID id for specific quote
     * @param userId facebook id of the user who favorited the quote
     */
    public static void addToFavoriteTable(String postID, String userId) {
        ReplaceableAttribute favoritedPostID = new ReplaceableAttribute("postID", postID, Boolean.FALSE);
        ReplaceableAttribute accName = new ReplaceableAttribute("likedBy", userId, Boolean.FALSE);

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(2);
        attrs.add(favoritedPostID);
        attrs.add(accName);

        PutAttributesRequest par = new PutAttributesRequest("Favorites", postID + "_likedBy_" + userId, attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * Adds a user and the user who will follow them to the Following table
     *
     * @param followedId the id of the person whose posts are going to be followed
     * @param followerId id of user who pressed the follow icon
     */
    public static void addToFollowingTable(String followedId, String followerId) {
        ReplaceableAttribute followedIdAttr = new ReplaceableAttribute("followedId", followedId, Boolean.FALSE);
        ReplaceableAttribute followerIdAttr = new ReplaceableAttribute("followerId", followerId, Boolean.FALSE);

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(2);
        attrs.add(followedIdAttr);
        attrs.add(followerIdAttr);

        PutAttributesRequest par = new PutAttributesRequest("Following", followedId + "_followedBy_" + followerId, attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * Check whether the user has followed a specific user
     *
     * @param posterId id of user who posted the quote and is now being followed
     * @param userId   id of the user who pressed the follow icon
     */
    public static boolean isFollowedByUser(String posterId, String userId) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Following where followedId = '" +
                posterId + "' and followerId = '" + userId + "'").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();
        return items.size() > 0;
    }

    public static void addPostIDToDomain(String postID, String tableName) {
        ReplaceableAttribute postIDAttribute = new ReplaceableAttribute("postID", postID, Boolean.FALSE);
        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(1);
        attrs.add(postIDAttribute);

        PutAttributesRequest par = new PutAttributesRequest(tableName, postID, attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * Add a quote to the Quotes Table
     *
     * @param quote quote to be added to the table
     */
    public static void addQuote(QuotePost quote) {
        ReplaceableAttribute quoteAttribute = new ReplaceableAttribute("quoteText", quote.getQuoteText(), Boolean.FALSE);
        ReplaceableAttribute authorAttribute = new ReplaceableAttribute("author", quote.getAuthorName(), Boolean.FALSE);
        ReplaceableAttribute timeAttribute = new ReplaceableAttribute("timestamp", "" + quote.getTimestamp(), Boolean.FALSE);
        ReplaceableAttribute fbNameAttribute = new ReplaceableAttribute("fbName", quote.getFbName(), Boolean.FALSE);
        ReplaceableAttribute fbIdAttribute = new ReplaceableAttribute("userId", quote.getUserId(), Boolean.FALSE);
        ReplaceableAttribute numFavorites = new ReplaceableAttribute("favorites", "0", Boolean.TRUE);

        Integer[] categories = quote.getCategories();

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(6 + categories.length);
        attrs.add(quoteAttribute);
        attrs.add(authorAttribute);
        attrs.add(timeAttribute);
        attrs.add(fbNameAttribute);
        attrs.add(fbIdAttribute);
        attrs.add(numFavorites);

        //add every category to the attribute - can have multiple values
        for (int i = 0; i < categories.length; i++) {
            switch (categories[i]) {
                case 0:
                    attrs.add(new ReplaceableAttribute("category", "advice", Boolean.FALSE));
                    break;
                case 1:
                    attrs.add(new ReplaceableAttribute("category", "funny", Boolean.FALSE));
                    break;
                case 2:
                    attrs.add(new ReplaceableAttribute("category", "inspirational", Boolean.FALSE));
                    break;
                case 3:
                    attrs.add(new ReplaceableAttribute("category", "love", Boolean.FALSE));
                    break;
                case 4:
                    attrs.add(new ReplaceableAttribute("category", "movie", Boolean.FALSE));
                    break;
                case 5:
                    attrs.add(new ReplaceableAttribute("category", "song", Boolean.FALSE));
                    break;
                default:
                    throw new IllegalArgumentException("Too many categories.");
            }
        }

        PutAttributesRequest par =
                new PutAttributesRequest(QUOTES, quote.getFbName().replace(" ", "") + quote.getTimestamp(), attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * @param myUserId User ID to return all quotes posted by them
     * @return itemNames of all the Quotes created by the user
     */
    public static List<String> getMyQuotesItemNames(String myUserId) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where userId = '" + myUserId +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(items.get(i).getName());
        }
        return itemNames;
    }

    /**
     * Retrieve the number of favorites a specific post has
     *
     * @param postId the post identifier to look up its number of favorites
     */
    public static int favCount(String postId) {
        SelectRequest selectRequest =
                new SelectRequest("select favorites from Quotes where itemName() = '" + postId + "'")
                        .withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();
        return Integer.parseInt(items.get(0).getAttributes().get(0).getValue());
    }

    /**
     * Check whether the user has favorited a specific post
     *
     * @param postId post identifier
     * @param userId user ID seeing the feed
     */
    public static boolean isFavoritedByUser(String postId, String userId) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Favorites where postID = '" + postId +
                "' and likedBy = '" + userId + "'").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();
        return items.size() > 0;
    }

    /**
     * Retrieve itemNames for all quotes that were not posted by the user
     *
     * @param myUserId user ID of the viewer so that their posts do not appear in their main feed
     */
    public static List<String> getFeedItemNames(String myUserId) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where userId != '" + myUserId +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);

        AmazonSimpleDBClient client = getInstance();
        if (client != null) {
            List<Item> items = client.select(selectRequest).getItems();

            List<String> itemNames = new ArrayList<String>();
            for (int i = 0; i < items.size(); i++) {
                itemNames.add((items.get(i)).getName());
            }
            return itemNames;
        } else return new ArrayList<String>();
    }

    /**
     * Retrieve quotes posted by posters the user is following
     *
     * @param myUserId facebook user ID
     */
    public static List<String> getFollowingFeedItemNames(String myUserId) {
        SelectRequest selectRequestNames = new SelectRequest("select followedId from Following where followerId = '" +
                myUserId + "'").withConsistentRead(true);
        List<Item> names = getInstance().select(selectRequestNames).getItems();

        // Work-around for no nested queries in SimpleDB
        String followedSet = "(";
        for (int j = 0; j < names.size(); j++) {
            followedSet += "'" + names.get(j).getAttributes().get(0).getValue() + "',";
        }
        followedSet = followedSet.substring(0, followedSet.length() - 1) + ")";

        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where userId in " + followedSet +
                " and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add((items.get(i)).getName());
        }
        return itemNames;
    }

    /**
     * Retrieve itemNames for all quotes that were not posted by the user in order of favorites
     */
    public static List<String> getPopularFeedItemNames() {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where favorites is not null " +
                "order by favorites desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(((Item) items.get(i)).getName());
        }
        return itemNames;
    }

    /**
     * Get quotes by a specific user
     *
     * @param userId ID of the user whose quotes are being looked up
     */
    public static List<String> getUserItemNames(String userId) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where userId = '" + userId +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(items.get(i).getName());
        }
        return itemNames;
    }

    /**
     * Get quotes favorited by a specific user
     *
     * @param userId ID of the user whose favorites are being looked up
     */
    public static List<String> getFavoriteFeedItemNames(String userId) {
        SelectRequest selectRequest = new SelectRequest("select postID from Favorites where likedBy = '" + userId +
                "' and postID is not null order by postID asc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(items.get(i).getAttributes().get(0).getValue());
        }
        return itemNames;
    }

    /**
     * Get quotes by a specific category
     *
     * @param category name of the quote category
     */
    public static List<String> getFeedItemNamesByCategory(String category) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where category = '" +
                category + "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(items.get(i).getName());
        }
        return itemNames;
    }

    /**
     * Get quotes in which query term appears in the quote, author, or poster name
     *
     * @param query the term to search by
     */
    public static List<String> getItemNamesBySearchQuery(String query) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where " +
                "(author like '%" + query + "%' or fbName like '%" + query + "%' or quoteText like '%" +
                query + "%') and " + "timestamp is not null order by timestamp desc limit 25").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(items.get(i).getName());
        }
        return itemNames;
    }
}
