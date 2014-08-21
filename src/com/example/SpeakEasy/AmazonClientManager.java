package com.example.SpeakEasy;

import android.content.SharedPreferences;
import android.util.Log;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.example.SpeakEasy.tvmclient.AmazonSharedPreferencesWrapper;
import com.example.SpeakEasy.tvmclient.AmazonTVMClient;
import com.example.SpeakEasy.tvmclient.Response;

/**
 * This class is used to get clients to the various AWS services.  Before accessing a client
 * the credentials should be checked to ensure validity.
 */
public class AmazonClientManager {
    private static final String LOG_TAG = "AmazonClientManager";

    private AmazonSimpleDBClient sdbClient = null;
    private SharedPreferences sharedPreferences = null;

    public AmazonClientManager(SharedPreferences settings) {
        this.sharedPreferences = settings;
    }

    public AmazonSimpleDBClient sdb() {
        validateCredentials();
        return sdbClient;
    }

    public boolean hasCredentials() {
        return PropertyLoader.getInstance().hasCredentials();
    }

    public Response validateCredentials() {
        Response ableToGetToken = Response.SUCCESSFUL;
        if (AmazonSharedPreferencesWrapper
                .areCredentialsExpired(this.sharedPreferences)) {
            synchronized (this) {
                if (AmazonSharedPreferencesWrapper
                        .areCredentialsExpired(this.sharedPreferences)) {
                    Log.i(LOG_TAG, "Credentials were expired.");
                    AmazonTVMClient tvm = new AmazonTVMClient(this.sharedPreferences,
                            PropertyLoader.getInstance().getTokenVendingMachineURL(),
                            PropertyLoader.getInstance().useSSL());
                    ableToGetToken = tvm.anonymousRegister();
                    if (ableToGetToken.requestWasSuccessful()) {
                        ableToGetToken = tvm.getToken();
                        if (ableToGetToken.requestWasSuccessful()) {
                            Log.i(LOG_TAG, "Creating New Credentials.");
                            initClients();
                        }
                    }
                }
            }
        } else if (sdbClient == null) {
            synchronized (this) {
                if (sdbClient == null) {
                    Log.i(LOG_TAG, "Creating New Credentials.");
                    initClients();
                }
            }
        }
        return ableToGetToken;
    }

    public void clearCredentials() {
        AmazonSharedPreferencesWrapper.wipe(this.sharedPreferences);
        sdbClient = null;
    }

    private void initClients() {
        AWSCredentials credentials = AmazonSharedPreferencesWrapper
                .getCredentialsFromSharedPreferences(this.sharedPreferences);
        Region region = Region.getRegion(Regions.US_EAST_1);
        sdbClient = new AmazonSimpleDBClient(credentials);
        sdbClient.setRegion(region);
    }
}
