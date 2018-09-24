package org.atlaslabs.newsbeuterspread.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ActivitySettingsBinding;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_BASE_URL;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_JS_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_PASSWORD;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_USERNAME;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    public enum Setting {
        PREFERENCE_NAME,
        PREFERENCE_BASE_URL,
        PREFERENCE_JS_ENABLE,
        PREFERENCE_USERNAME,
        PREFERENCE_PASSWORD
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE);
        if(preferences.contains(PREFERENCE_BASE_URL.name()))
            binding.baseURLText.setText(preferences.getString(PREFERENCE_BASE_URL.name(), null));
        binding.jsEnableCheckbox.setChecked(preferences.getBoolean(PREFERENCE_JS_ENABLE.name(), false));
        binding.usernameText.setText(preferences.getString(PREFERENCE_USERNAME.name(), ""));
        binding.passwordText.setText(preferences.getString(PREFERENCE_PASSWORD.name(), ""));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                String baseURL = binding.baseURLText.getText().toString();
                String username = binding.usernameText.getText().toString();
                String password = binding.passwordText.getText().toString();
                getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).edit()
                        .putString(PREFERENCE_BASE_URL.name(), baseURL)
                        .putString(PREFERENCE_USERNAME.name(), username)
                        .putString(PREFERENCE_PASSWORD.name(), password)
                        .putBoolean(PREFERENCE_JS_ENABLE.name(), binding.jsEnableCheckbox.isChecked())
                        .apply();
                Intent intent = new Intent();
                intent.putExtra(PREFERENCE_BASE_URL.name(), baseURL);
                intent.putExtra(PREFERENCE_USERNAME.name(), username);
                intent.putExtra(PREFERENCE_PASSWORD.name(), password);
                intent.putExtra(PREFERENCE_JS_ENABLE.name(), binding.jsEnableCheckbox.isChecked());
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
