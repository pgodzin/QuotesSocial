package com.example.SpeakEasy;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;

public class FBUtil {

    public static void shareToFB(Context c, Activity act, String quoteText, UiLifecycleHelper uiHelper) {

        if (FacebookDialog.canPresentOpenGraphActionDialog(c.getApplicationContext(),
                FacebookDialog.OpenGraphActionDialogFeature.OG_ACTION_DIALOG)) {
            OpenGraphObject quote = OpenGraphObject.Factory.createForPost
                    (OpenGraphObject.class, "speakeasydevfest:post", "I posted a new quote!",
                            "http://i.imgur.com/ec9p33P.jpg", null, "\"" + quoteText + "\"");
            OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
            action.setProperty("quote", quote);
            action.setType("speakeasydevfest:post");

            FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(act, action, "quote")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else {
            Toast.makeText(c, "Facebook not available", Toast.LENGTH_SHORT).show();
        }
    }
}
