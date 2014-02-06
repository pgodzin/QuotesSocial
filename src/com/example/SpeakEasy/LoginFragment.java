package com.example.SpeakEasy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private UiLifecycleHelper uiHelper;

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
        if (state.isOpened()) {

            /*final SharedPreferences prefs = getActivity().getSharedPreferences(
                    "fbInfo", Context.MODE_PRIVATE);
            if (session != null && session.getState().isOpened()){
                Log.i("sessionToken", session.getAccessToken());
                Log.i("sessionTokenDueDate", session.getExpirationDate().toLocaleString());
            }

            new Request(session, "/me?fields=name", null, HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            JSONObject graphResponse = response
                                    .getGraphObject()
                                    .getInnerJSONObject();
                            try {
                                prefs.edit().putString("name", graphResponse.getString("name")).commit();

                            } catch (JSONException e) {
                                Log.i(TAG,
                                        "JSON error " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();

            new Request(session, "/me?fields=quotes", null, HttpMethod.GET,
                    new Request.Callback() {
                        public void onCompleted(Response response) {
                            JSONObject graphResponse = response
                                    .getGraphObject()
                                    .getInnerJSONObject();
                            try {
                                prefs.edit().putString("quotes", graphResponse.getString("quotes")).commit();

                            } catch (JSONException e) {
                                Log.i(TAG,
                                        "JSON error " + e.getMessage());
                            }
                        }
                    }
            ).executeAsync();*/

            Intent i = new Intent(getActivity(), MainPage.class);
            startActivity(i);
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

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