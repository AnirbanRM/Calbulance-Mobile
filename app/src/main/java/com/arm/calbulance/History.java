package com.arm.calbulance;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.WindowDecorActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

public class History extends Fragment {

    TabLayout tab_selector;
    FrameLayout history_frame;
    public String EMAIL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_history,null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tab_selector = view.findViewById(R.id.tab_selector);
        history_frame = view.findViewById(R.id.history_frame);
        tab_selector.addOnTabSelectedListener(history_tab_changed);

        getChildFragmentManager().beginTransaction().replace(R.id.history_frame,new History_Ambulances()).commit();

        List<Fragment> f_arr = getFragmentManager().getFragments();
        EMAIL = f_arr.get(0).getArguments().getString("EMAIL");
    }
    TabLayout.OnTabSelectedListener history_tab_changed = new TabLayout.OnTabSelectedListener() {
        Fragment f;
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if(tab.getText().toString().equals("Ambulances")) {
                f = new History_Ambulances();
            }
            else if(tab.getText().toString().equals("Appointments")) {
                f = new History_Appointment();
            }
            getChildFragmentManager().beginTransaction().replace(R.id.history_frame,f).commit();
        }
        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }
        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };
}