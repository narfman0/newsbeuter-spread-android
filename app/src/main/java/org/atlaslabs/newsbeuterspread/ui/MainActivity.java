package org.atlaslabs.newsbeuterspread.ui;

import android.content.Context;
import android.content.Intent;
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

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ActivityMainBinding;
import org.atlaslabs.newsbeuterspread.models.Item;
import org.atlaslabs.newsbeuterspread.network.NewsbeuterSpreadAPI;
import org.atlaslabs.newsbeuterspread.network.RestUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_BASE_URL;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SETTINGS = 1;
    private ActivityMainBinding binding;
    private NewsbeuterSpreadAPI api = null;
    private String baseURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        binding.itemsList.setLayoutManager(new LinearLayoutManager(this));
        binding.itemsRefresh.setOnRefreshListener(this::updateAPI);

        // update items list
        if(getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).contains(PREFERENCE_BASE_URL.name())) {
            baseURL = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).getString(PREFERENCE_BASE_URL.name(), null);
            updateAPI();
        }
    }

    private void updateAPI(){
        binding.itemsRefresh.setRefreshing(true);
        api = RestUtil.createAPI(baseURL);
        api.getUnread()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    loadItems(response.items);
                    binding.itemsRefresh.setRefreshing(false);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTINGS) {
            if (resultCode == RESULT_OK) {
                String currentBaseURL = data.getStringExtra(PREFERENCE_BASE_URL.name());
                // if the user updated the url, fetch list!
                if(!TextUtils.equals(currentBaseURL, baseURL)) {
                    baseURL = currentBaseURL;
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
