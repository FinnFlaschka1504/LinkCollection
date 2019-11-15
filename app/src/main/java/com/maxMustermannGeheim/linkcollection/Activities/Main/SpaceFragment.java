package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxMustermannGeheim.linkcollection.Activities.Settings;

public class SpaceFragment extends Fragment {

    private int layoutId;
    public static Settings.Space currentSpace;

    public SpaceFragment() {
        if (currentSpace == null)
            return;
        layoutId = currentSpace.getFragmentLayoutId();
    }

    public SpaceFragment(int layoutId) {
        this.layoutId = layoutId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutId, container, false);
    }
}
