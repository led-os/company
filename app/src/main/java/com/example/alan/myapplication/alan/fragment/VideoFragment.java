package com.example.alan.myapplication.alan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alan.myapplication.R;


/**
 * Created by Alan on 2018/1/20.
 */

public class VideoFragment extends ABaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video, null);

        return view;
    }
}
