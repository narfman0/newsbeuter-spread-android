package org.atlaslabs.newsbeuterspread.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ActivitySettingsBinding;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_BASE_URL;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;

public class SettingsActivity extends AppCompatActivity {
    public enum Setting {
        PREFERENCE_NAME,
        PREFERENCE_BASE_URL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.saveButton.setOnClickListener(v -> {
            String baseURL = binding.editText.getText().toString();
            getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).edit()
                    .putString(Setting.PREFERENCE_BASE_URL.name(), baseURL)
                    .apply();
            Intent intent = new Intent();
            intent.putExtra(PREFERENCE_BASE_URL.name(), baseURL);
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
    }
}
