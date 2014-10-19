package com.example.SpeakEasy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.facebook.UiLifecycleHelper;

public class SearchListFragment extends MainPageListFragment {
    private String query;
    private String fragmentName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(this.getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle(query);
        new Thread(new Runnable() {
            public void run() {
                if (!getArguments().isEmpty()) {
                    query = getArguments().getString("query");
                    fragmentName = getArguments().getString("fragmentName");
                    itemNames = SimpleDB.getItemNamesBySearchQuery(query, fragmentName);
                    adapter = new MySimpleArrayAdapter(getActivity(), itemNames);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (itemNames.size() == 0) {
                                Toast.makeText(getActivity().getApplicationContext(), "No quotes available by this search term",
                                        Toast.LENGTH_LONG).show();
                            }
                            setListAdapter(adapter);
                        }
                    });
                }
            }
        }).start();

        return inflater.inflate(R.layout.main_listfragment, container, false);
    }

    @Override
    public String getFragmentTitle() {
        return "Advice Quotes";
    }
}