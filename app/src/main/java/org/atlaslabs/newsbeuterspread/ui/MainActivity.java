package org.atlaslabs.newsbeuterspread.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jcraft.jsch.Session;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ActivityMainBinding;
import org.atlaslabs.newsbeuterspread.models.Item;
import org.atlaslabs.newsbeuterspread.network.NewsbeuterSpreadAPI;
import org.atlaslabs.newsbeuterspread.network.RestUtil;
import org.atlaslabs.newsbeuterspread.network.SSHUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_BASE_URL;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_JS_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_HOST;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_PASSWORD;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_SSH_USERNAME;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SETTINGS = 1;
    private CompositeDisposable disposable;
    private ActivityMainBinding binding;
    private NewsbeuterSpreadAPI api = null;
    private String baseURL;
    private boolean jsEnabled, sshEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        disposable = new CompositeDisposable();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        binding.itemsList.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsRefresh.setOnRefreshListener(this::updateAPI);

        // update items list
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE);
        jsEnabled = preferences.getBoolean(PREFERENCE_JS_ENABLE.name(), false);
        sshEnabled = preferences.getBoolean(PREFERENCE_SSH_ENABLE.name(), false);
        if(preferences.contains(PREFERENCE_BASE_URL.name())) {
            baseURL = preferences.getString(PREFERENCE_BASE_URL.name(), null);
            updateAPI();
        }
    }

    @Override
    protected void onDestroy(){
        disposable.dispose();
        super.onDestroy();
    }

    public void updateAPI(){
        if(baseURL == null){
            Toast.makeText(getApplicationContext(), "Base URL null, please update in settings!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if(sshEnabled) {
            SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE);
            String host = preferences.getString(PREFERENCE_SSH_HOST.name(), null);
            String user = preferences.getString(PREFERENCE_SSH_USERNAME.name(), null);
            String pass = preferences.getString(PREFERENCE_SSH_PASSWORD.name(), null);
            try {
                Schedulers.newThread().scheduleDirect(() -> {
                    try {
                        disposable.add(SSHUtil.connect(getApplicationContext(), host, user, pass));
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),
                                "Failed to establish ssh connection with error: " +
                                        e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),
                        "Failed to establish ssh connection with error: " +
                                e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        }
        binding.itemsRefresh.setRefreshing(true);
        api = RestUtil.createAPI(baseURL);
        disposable.add(api.getUnread()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if(response.items.isEmpty()){
                        binding.itemsList.setVisibility(View.GONE);
                        binding.itemsCaughtUp.setVisibility(View.VISIBLE);
                    }else{
                        binding.itemsList.setVisibility(View.VISIBLE);
                        binding.itemsCaughtUp.setVisibility(View.GONE);
                    }
                    loadItems(response.items);
                    binding.itemsRefresh.setRefreshing(false);
                }, e -> {
                    String text = "An error has occurred fetching unread: " + e;
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
                            .show();
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                String currentBaseURL = data.getStringExtra(PREFERENCE_BASE_URL.name());
                boolean currentJSEnabled = data.getBooleanExtra(PREFERENCE_JS_ENABLE.name(), false);
                boolean currentSSHEnabled = data.getBooleanExtra(PREFERENCE_SSH_ENABLE.name(), false);
                // if the user updated the url, fetch list!
                if(!TextUtils.equals(currentBaseURL, baseURL) || currentJSEnabled != jsEnabled ||
                        currentSSHEnabled != sshEnabled) {
                    baseURL = currentBaseURL;
                    jsEnabled = currentJSEnabled;
                    sshEnabled = currentSSHEnabled;
                    updateAPI();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateAPI();
                return true;
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    public NewsbeuterSpreadAPI getAPI(){
        return api;
    }

    private void loadItems(List<Item> items) {
        binding.itemsList.setAdapter(new ItemAdapter(this, new ArrayList<>(items)));
    }
}
