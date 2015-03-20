package main.java.com.example.SpeakEasy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment that logs in the user
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    public static boolean isNetworkAvailable(Activity context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean available = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        if (!available) {
            Toast.makeText(context, "No internet service currently available.", Toast.LENGTH_LONG).show();
        }
        return available;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        return view;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened() && isNetworkAvailable(getActivity())) {
            final SharedPreferences prefs = getActivity().getSharedPreferences(
                    "fbInfo", Context.MODE_PRIVATE);
            if (session != null && session.getState().isOpened()) {
                Log.i("sessionToken", session.getAccessToken());
                Log.i("sessionTokenDueDate", session.getExpirationDate().toLocaleString());
            }
            Bundle params = new Bundle();
            if (session != null) {
                params.putString("access_token", session.getAccessToken());
            }
            new Request(session, "/me", params, HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            JSONObject graphResponse = response
                                    .getGraphObject()
                                    .getInnerJSONObject();
                            try {
                                prefs.edit().putString("name", graphResponse.getString("name")).commit();
                                //Toast.makeText(getActivity(), "Welcome " + graphResponse.getString("name"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Log.i(TAG, "JSON error " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();

            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null) {
                        try {
                            String userId = user.getId();
                            prefs.edit().putString("id", userId).apply();
                            prefs.edit().putString("profile_url", "https://graph.facebook.com/"
                                    + user.getId() + "/picture?type=large").commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).executeAsync();

            Intent i = new Intent(getActivity(), MainPage.class);
            startActivity(i);
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode,
                resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}