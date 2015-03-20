package main.java.com.example.SpeakEasy;

import android.util.Log;

import java.util.Properties;

public class PropertyLoader {

    private static PropertyLoader instance = null;
    private boolean hasCredentials = false;
    private String tokenVendingMachineURL = null;
    private boolean useSSL = false;
    private String verifiedEmail = null;

    public PropertyLoader() {
        try {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream("AwsCredentials.properties"));

            this.tokenVendingMachineURL = properties.getProperty("tokenVendingMachineURL");
            this.useSSL = Boolean.parseBoolean(properties.getProperty("useSSL"));
            this.verifiedEmail = properties.getProperty("VERIFIED_EMAIL");

            if (this.tokenVendingMachineURL == null || this.tokenVendingMachineURL.equals("") || this.tokenVendingMachineURL.equals("CHANGEME")) {
                this.tokenVendingMachineURL = null;
                this.useSSL = false;
                this.hasCredentials = false;
            } else {
                this.hasCredentials = true;
            }
        } catch (Exception exception) {
            Log.e("PropertyLoader", "Unable to read property file.");
        }
    }

    public static PropertyLoader getInstance() {
        if (instance == null) {
            instance = new PropertyLoader();
        }
        return instance;
    }

    public boolean hasCredentials() {
        return this.hasCredentials;
    }

    public String getTokenVendingMachineURL() {
        return this.tokenVendingMachineURL;
    }

    public boolean useSSL() {
        return this.useSSL;
    }

    public String getVerifiedEmail() {
        return this.verifiedEmail;
    }
}
