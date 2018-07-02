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
import android.view.View;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ActivitySettingsBinding;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_BASE_URL;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_JS_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_HOST;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_PASSWORD;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_USERNAME;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    public enum Setting {
        PREFERENCE_NAME,
        PREFERENCE_BASE_URL,
        PREFERENCE_JS_ENABLE,
        PREFERENCE_SSH_ENABLE,
        PREFERENCE_SSH_HOST,
        PREFERENCE_SSH_USERNAME,
        PREFERENCE_SSH_PASSWORD
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE);
        if(preferences.contains(PREFERENCE_BASE_URL.name()))
            binding.baseURLText.setText(preferences.getString(PREFERENCE_BASE_URL.name(), null));
        binding.jsEnableCheckbox.setChecked(preferences.getBoolean(PREFERENCE_JS_ENABLE.name(), false));

        boolean sshEnabled = preferences.getBoolean(PREFERENCE_SSH_ENABLE.name(), false);
        binding.sshEnableCheckbox.setChecked(sshEnabled);
        binding.sshEnableCheckbox.setOnClickListener(v ->
            updateSSHChecked(binding.sshEnableCheckbox.isChecked())
        );
        updateSSHChecked(sshEnabled);
        if(preferences.contains(PREFERENCE_SSH_HOST.name()))
            binding.sshHostText.setText(preferences.getString(PREFERENCE_SSH_HOST.name(), null));
        if(preferences.contains(PREFERENCE_SSH_USERNAME.name()))
            binding.sshUsernameText.setText(preferences.getString(PREFERENCE_SSH_USERNAME.name(), null));
        if(preferences.contains(PREFERENCE_SSH_PASSWORD.name()))
            binding.sshPasswordText.setText(preferences.getString(PREFERENCE_SSH_PASSWORD.name(), null));

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
                getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).edit()
                        .putString(PREFERENCE_BASE_URL.name(), baseURL)
                        .putBoolean(PREFERENCE_JS_ENABLE.name(), binding.jsEnableCheckbox.isChecked())
                        .putBoolean(PREFERENCE_SSH_ENABLE.name(), binding.sshEnableCheckbox.isChecked())
                        .putString(PREFERENCE_SSH_HOST.name(), binding.sshHostText.getText().toString())
                        .putString(PREFERENCE_SSH_USERNAME.name(), binding.sshUsernameText.getText().toString())
                        .putString(PREFERENCE_SSH_PASSWORD.name(), binding.sshPasswordText.getText().toString())
                        .apply();
                Intent intent = new Intent();
                intent.putExtra(PREFERENCE_BASE_URL.name(), baseURL);
                intent.putExtra(PREFERENCE_JS_ENABLE.name(), binding.jsEnableCheckbox.isChecked());
                intent.putExtra(PREFERENCE_SSH_ENABLE.name(), binding.sshEnableCheckbox.isChecked());
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSSHChecked(boolean sshEnabled){
        binding.sshHostText.setVisibility(sshEnabled ? View.VISIBLE : View.GONE);
        binding.sshUsernameText.setVisibility(sshEnabled ? View.VISIBLE : View.GONE);
        binding.sshPasswordText.setVisibility(sshEnabled ? View.VISIBLE : View.GONE);
    }
}
