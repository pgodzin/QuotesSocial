// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package main.java.com.example.SpeakEasy;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

/**
 * Custom list adapter to embed AdMob ads in a ListView at the top
 * and bottom of the screen.
 */
public class AdListAdapter extends BaseAdapter {
    private static final String LOG_TAG = "AdListAdapter";

    private final Activity activity;
    private final BaseAdapter delegate;

    public AdListAdapter(Activity activity, BaseAdapter delegate) {
        this.activity = activity;
        this.delegate = delegate;
    }

    /**
     * Creates an ad request. It will be a test request if test mode is enabled.
     *
     * @return An AdRequest to use when loading an ad.
     */
    public static AdRequest createAdRequest() {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        //if (AdCatalog.isTestMode) {
        // This call will add the emulator as a test device. To add a physical
        // device for testing, pass in your hashed device ID, which can be found
        // in the LogCat output when loading an ad on your device.
        //adRequestBuilder.addTestDevice("9BF341CF3BDAF18752C5E61244B715AE");
        adRequestBuilder.addTestDevice("F443A6138710A2F470587A5C704E08A7");
        return adRequestBuilder.build();
    }

    @Override
    public int getCount() {
        // Total count includes list items and ads.
        return delegate.getCount() + (delegate.getCount() + 3) / 5;
    }

    @Override
    public Object getItem(int position) {
        // Return null if an item is an ad.  Otherwise return the delegate item.
        if (isItemAnAd(position)) {
            return null;
        }
        return delegate.getItem(getOffsetPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if ((position % 5) == 2) {
            if (convertView instanceof AdView) {
                // Don’t instantiate new AdView, reuse old one
                return convertView;
            } else {
                // Create a new AdView
                AdView adView = new AdView(activity);
                adView.setAdUnitId(activity.getResources().getString(R.string.banner_ad_unit_id_test));
                adView.setAdSize(AdSize.BANNER);
                adView.loadAd(createAdRequest());
                adView.setAdListener(new LogAndToastAdListener(activity));

                // Convert the default layout parameters so that they play nice with
                // ListView.

                float density = activity.getResources().getDisplayMetrics().density;
                int height = Math.round(AdSize.BANNER.getHeight() * density);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        height);

                adView.setLayoutParams(params);
                return adView;
            }
        } else {
            if (convertView instanceof AdView) {
                convertView = null;
            }
            // Offload displaying other items to the delegate
            return delegate.getView(position - ((position + 3) / 5),
                    convertView, parent);
        }
    }

    @Override
    public int getViewTypeCount() {
        return delegate.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isItemAnAd(position)) {
            return delegate.getViewTypeCount();
        } else {
            return delegate.getItemViewType(getOffsetPosition(position));
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return (!isItemAnAd(position)) && delegate.isEnabled(getOffsetPosition(position));
    }

    private boolean isItemAnAd(int position) {
        // Place an ad at the first and last list view positions.
        return (position == 0 || position == (getCount() - 1));
    }

    private int getOffsetPosition(int position) {
        return position - 1;
    }

    private class LogAndToastAdListener extends AdListener {

        Activity activity;

        public LogAndToastAdListener(Activity activity) {
            this.activity = activity;
        }

        /**
         * Called when an ad is clicked and about to return to the application.
         */
        @Override
        public void onAdClosed() {
            Log.d(LOG_TAG, "onAdClosed");
            //Toast.makeText(activity, "onAdClosed", Toast.LENGTH_SHORT).show();
        }

        /**
         * Called when an ad failed to load.
         */
        @Override
        public void onAdFailedToLoad(int error) {
            String message = "onAdFailedToLoad: " + getErrorReason(error);
            Log.d(LOG_TAG, message);
            //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }

        /**
         * Called when an ad is clicked and going to start a new Activity that will
         * leave the application (e.g. breaking out to the Browser or Maps
         * application).
         */
        @Override
        public void onAdLeftApplication() {
            Log.d(LOG_TAG, "onAdLeftApplication");
            //Toast.makeText(activity, "onAdLeftApplication", Toast.LENGTH_SHORT).show();
        }

        /**
         * Called when an Activity is created in front of the app (e.g. an
         * interstitial is shown, or an ad is clicked and launches a new Activity).
         */
        @Override
        public void onAdOpened() {
            Log.d(LOG_TAG, "onAdOpened");
            //Toast.makeText(activity, "onAdOpened", Toast.LENGTH_SHORT).show();
        }

        /**
         * Called when an ad is loaded.
         */
        @Override
        public void onAdLoaded() {
            Log.d(LOG_TAG, "onAdLoaded");
            //Toast.makeText(activity, "onAdLoaded", Toast.LENGTH_SHORT).show();
        }

        private String getErrorReason(int errorCode) {
            String errorReason = "";
            switch (errorCode) {
                case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                    errorReason = "Internal error";
                    break;
                case AdRequest.ERROR_CODE_INVALID_REQUEST:
                    errorReason = "Invalid request";
                    break;
                case AdRequest.ERROR_CODE_NETWORK_ERROR:
                    errorReason = "Network Error";
                    break;
                case AdRequest.ERROR_CODE_NO_FILL:
                    errorReason = "No fill";
                    break;
            }
            return errorReason;
        }

    }
}

