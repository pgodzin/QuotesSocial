package main.java.com.example.SpeakEasy;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 */
public class Widget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String userId = context.getSharedPreferences("fbInfo", Context.MODE_PRIVATE).getString("id", "");
        String category = WidgetConfigureActivity.loadCategoryPref(context, appWidgetId);
        List<String> itemNames;

        if (category.equals("All")) {
            itemNames = SimpleDB.getFeedItemNames(userId);
        } else if (category.equals("Following")) {
            itemNames = SimpleDB.getFollowingFeedItemNames(userId);
        } else {
            itemNames = SimpleDB.getFeedItemNamesByCategory(category.toLowerCase());
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        if (itemNames.size() > 0) {
            HashMap<String, String> attrMap = SimpleDB.getAttributesForItem("Quotes", itemNames.get(0));
            views.setTextViewText(R.id.widgetQuote, "\"" + attrMap.get("quoteText") + "\"  ");
            views.setTextViewText(R.id.widgetAuthor, "- " + attrMap.get("author"));
        }
        // Create an Intent to launch MainPage Activity
        Intent intent = new Intent(context, MainPage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.widgetView, pendingIntent);

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            if (context != null) {
                updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WidgetConfigureActivity.deleteCategoryPref(context, appWidgetIds[i]);
        }
    }
}


