package com.maxMustermannGeheim.linkcollection.Activities.Main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.maxMustermannGeheim.linkcollection.Activities.Settings;
import com.maxMustermannGeheim.linkcollection.R;

public class SpaceFragment extends Fragment {

    private int layoutId = -1;
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
        if (layoutId == -1) {
            layoutId = R.layout.loading_screen;
            MainActivity.isLoadingLayout = true;
        }
        return inflater.inflate(layoutId, container, false);
    }
}
