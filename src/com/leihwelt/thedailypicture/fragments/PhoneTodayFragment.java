package com.leihwelt.thedailypicture.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leihwelt.thedailypicture.R;

/**
 * @author atla This fragment is used for the center fragment of the pager it
 *         displays the TodayPictureFragment as well as the PastDaysFragment
 */
public class PhoneTodayFragment extends Fragment {

    private TodayPictureFragment todayPictureFragment;

    public PhoneTodayFragment() {
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.phone_today_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.todayPictureFragment = (TodayPictureFragment) this.getActivity().getFragmentManager()
                .findFragmentById(R.id.today_picture_fragment);
    }

    public TodayPictureFragment getTodayPictureFragment() {
        return this.todayPictureFragment;
    }

}
