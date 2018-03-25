package org.atlaslabs.newsbeuterspread.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;

    public enum Setting {
        PREFERENCE_NAME,
        PREFERENCE_BASE_URL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
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
                        .putString(Setting.PREFERENCE_BASE_URL.name(), baseURL)
                        .apply();
                Intent intent = new Intent();
                intent.putExtra(PREFERENCE_BASE_URL.name(), baseURL);
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
