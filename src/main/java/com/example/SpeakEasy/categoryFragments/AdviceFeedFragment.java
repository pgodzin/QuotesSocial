package main.java.com.example.SpeakEasy.categoryFragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.UiLifecycleHelper;

import main.java.com.example.SpeakEasy.MainPageListFragment;
import main.java.com.example.SpeakEasy.R;
import main.java.com.example.SpeakEasy.SimpleDB;

/**
 * ListFragment that displays all the quotes posted with an 'advice' category tag
 */
public class AdviceFeedFragment extends MainPageListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        uiHelper = new UiLifecycleHelper(mActivity, null);
        uiHelper.onCreate(savedInstanceState);
        getActivity().setTitle(getFragmentTitle());
        if (isNetworkAvailable(mActivity)) {
            new Thread(new Runnable() {
                public void run() {
                    itemNames = SimpleDB.getFeedItemNamesByCategory("advice");
                    adapter = new MainPageListFragment.MySimpleArrayAdapter(mActivity, itemNames);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setListAdapter(adapter);

                        }
                    });
                }
            }).start();
        }
        return inflater.inflate(R.layout.main_listfragment, container, false);
    }

    /**
     * Called when the listView is pulled down for the data to refresh
     */
    @Override
    public void onRefresh() {
        if (isNetworkAvailable(mActivity)) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        public void run() {
                            itemNames = SimpleDB.getFeedItemNamesByCategory("advice");
                            adapter = new MySimpleArrayAdapter(mActivity, itemNames);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setListAdapter(adapter);
                                    swipeLayout.setRefreshing(false);
                                }
                            });
                        }
                    }).start();
                }
            }, 0);
        }
    }

    @Override
    public String getFragmentTitle() {
        return "Advice Quotes";
    }
}
