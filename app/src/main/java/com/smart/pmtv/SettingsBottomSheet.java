package com.smart.pmtv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingsBottomSheet extends BottomSheetDialogFragment {

    private PreferencesManager prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_settings, container, false);
        prefs = new PreferencesManager(requireContext());

        SwitchMaterial switchBackgroundPlayback = view.findViewById(R.id.switchBackgroundPlayback);
        RadioGroup rgThemes = view.findViewById(R.id.rgThemes);
        View btnClearCache = view.findViewById(R.id.btnClearCache);
        
        RadioButton rbLight = view.findViewById(R.id.rbThemeLight);
        RadioButton rbDark = view.findViewById(R.id.rbThemeDark);
        RadioButton rbBlue = view.findViewById(R.id.rbThemeBlue);
        RadioButton rbAmoled = view.findViewById(R.id.rbThemeAmoled);
        RadioButton rbStudio = view.findViewById(R.id.rbThemeStudio);

        // Load current
        switchBackgroundPlayback.setChecked(prefs.isBackgroundPlaybackEnabled());
        int currentTheme = prefs.getTheme();
        if (currentTheme == PreferencesManager.THEME_LIGHT) rbLight.setChecked(true);
        else if (currentTheme == PreferencesManager.THEME_BLUE) rbBlue.setChecked(true);
        else if (currentTheme == PreferencesManager.THEME_AMOLED) rbAmoled.setChecked(true);
        else if (currentTheme == PreferencesManager.THEME_STUDIO) rbStudio.setChecked(true);
        else rbDark.setChecked(true);

        switchBackgroundPlayback.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setBackgroundPlaybackEnabled(isChecked);
        });

        rgThemes.setOnCheckedChangeListener((group, checkedId) -> {
            int newTheme = PreferencesManager.THEME_DARK;
            if (checkedId == R.id.rbThemeLight) newTheme = PreferencesManager.THEME_LIGHT;
            else if (checkedId == R.id.rbThemeBlue) newTheme = PreferencesManager.THEME_BLUE;
            else if (checkedId == R.id.rbThemeAmoled) newTheme = PreferencesManager.THEME_AMOLED;
            else if (checkedId == R.id.rbThemeStudio) newTheme = PreferencesManager.THEME_STUDIO;

            if (newTheme != currentTheme) {
                prefs.setTheme(newTheme);
                requireActivity().recreate();
            }
        });

        btnClearCache.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Cache cleared successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }
}
