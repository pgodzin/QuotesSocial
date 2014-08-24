package com.example.SpeakEasy;

import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Util class for database transactions
 */
public class SimpleDB {
    private static String nextToken = null;
    private static int prevNumDomains = 0;
    public static final String QUOTES = "Quotes";

    public static AmazonSimpleDBClient getInstance() {
        if (MainPage.clientManager != null)
            return MainPage.clientManager.sdb();
        else return HomePage.clientManager.sdb();
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
            itemNames[i] = ((Item) items.get(i)).getName();
        }
        return itemNames;
    }

    /**
     * A map of attributes for a specific item in a table
     *
     * @param domainName table name
     * @param itemName   itemName for the item
     * @return
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
     * Adds a quote and the user who favorited it to the Favorites table
     *
     * @param postID      id for specific quote
     * @param accountName user name
     */
    public static void addToFavoriteTable(String postID, String accountName) {
        ReplaceableAttribute favoritedPostID = new ReplaceableAttribute("postID", postID, Boolean.FALSE);
        ReplaceableAttribute accName = new ReplaceableAttribute("likedBy", accountName, Boolean.FALSE);

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(2);
        attrs.add(favoritedPostID);
        attrs.add(accName);

        PutAttributesRequest par = new PutAttributesRequest("Favorites", postID + "_likedBy_" + accountName, attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * Adds a user and the user who will follow them to the Following table
     *
     * @param nameToFollow the name of the person whose post are going to be followed
     * @param followerName name of user who pressed the follow icon
     */
    public static void addToFollowingTable(String nameToFollow, String followerName) {
        ReplaceableAttribute followedName = new ReplaceableAttribute("followedName", nameToFollow, Boolean.FALSE);
        ReplaceableAttribute followedBy = new ReplaceableAttribute("followedBy", followerName, Boolean.FALSE);

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(2);
        attrs.add(followedName);
        attrs.add(followedBy);

        PutAttributesRequest par = new PutAttributesRequest("Following", nameToFollow + "_followedBy_" + followerName, attrs);
        try {
            getInstance().putAttributes(par);
        } catch (Exception exception) {
            System.out.println("EXCEPTION = " + exception);
        }
    }

    /**
     * Check whether the user has followed a specific user
     *
     * @param posterName name of user who posted the quote and is now being followed
     * @param userName   name of the user who pressed the follow icon
     * @return
     */
    public static boolean isFollowedByUser(String posterName, String userName) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Following where followedName = '" +
                posterName + "' and followedBy = '" + userName + "'").withConsistentRead(true);
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
     * @param quote
     */
    public static void addQuote(QuotePost quote) {
        ReplaceableAttribute quoteAttribute = new ReplaceableAttribute("quoteText", quote.getQuoteText(), Boolean.FALSE);
        ReplaceableAttribute authorAttribute = new ReplaceableAttribute("author", quote.getAuthorName(), Boolean.FALSE);
        ReplaceableAttribute timeAttribute = new ReplaceableAttribute("timestamp", "" + quote.getTimestamp(), Boolean.FALSE);
        ReplaceableAttribute fbNameAttribute = new ReplaceableAttribute("fbName", quote.getFbName(), Boolean.FALSE);
        ReplaceableAttribute numFavorites = new ReplaceableAttribute("favorites", "0", Boolean.TRUE);

        ArrayList<Integer> categories = quote.getCategories();
        int numCategories = categories.size();

        List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>(5 + numCategories);
        attrs.add(quoteAttribute);
        attrs.add(authorAttribute);
        attrs.add(timeAttribute);
        attrs.add(fbNameAttribute);
        attrs.add(numFavorites);

        //add every category to the attribute - can have multiple values
        for (int i = 0; i < numCategories; i++) {
            switch (categories.get(i)) {
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
     * @param myName
     * @return itemNames of all the Quotes created by the user to be shown in the HomePage
     */
    public static List<String> getMyQuotesItemNames(String myName) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where fbName = '" + myName +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(((Item) items.get(i)).getName());
        }
        return itemNames;
    }

    /**
     * Retrieve the number of favorites a specific post has
     *
     * @param postId
     * @return
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
     * @param postId
     * @param name
     * @return
     */
    public static boolean isFavoritedByUser(String postId, String name) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Favorites where postID = '" + postId +
                "' and likedBy = '" + name + "'").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();
        return items.size() > 0;
    }

    /**
     * Retrieve itemNames for all quotes that were not posted by the user
     *
     * @param myName
     * @return
     */
    public static List<String> getFeedItemNames(String myName) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where fbName != '" + myName +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add((items.get(i)).getName());
        }
        return itemNames;
    }

    /**
     * Retrieve quotes posted by posters the user is following
     *
     * @param myName
     * @return
     */
    public static List<String> getFollowingFeedItemNames(String myName) {
        //Work-around for no nested queries in SimpleDB
        SelectRequest selectRequestNames = new SelectRequest("select followedName from Following where followedBy = '" +
                myName + "'").withConsistentRead(true);
        List<Item> names = getInstance().select(selectRequestNames).getItems();

        String set = "(";
        for (int j = 0; j < names.size(); j++) {
            set += "'" + names.get(j).getAttributes().get(0).getValue() + "',";
        }
        set = set.substring(0, set.length() - 1) + ")";

        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where fbName in " + set +
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
     *
     * @param myName
     * @return
     */
    public static List<String> getPopularFeedItemNames(String myName) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where fbName != '" + myName +
                "' and favorites is not null order by favorites desc").withConsistentRead(true);
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
     * @param name
     * @return
     */
    public static List<String> getUserItemNamesByCategory(String name) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where fbName = '" + name +
                "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(((Item) items.get(i)).getName());
        }
        return itemNames;
    }


    /**
     * Get quotes by a specific category
     *
     * @param category
     * @return
     */
    public static List<String> getFeedItemNamesByCategory(String category) {
        SelectRequest selectRequest = new SelectRequest("select itemName() from Quotes where category = '" +
                category + "' and timestamp is not null order by timestamp desc").withConsistentRead(true);
        List<Item> items = getInstance().select(selectRequest).getItems();

        List<String> itemNames = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            itemNames.add(((Item) items.get(i)).getName());
        }
        return itemNames;
    }
}
