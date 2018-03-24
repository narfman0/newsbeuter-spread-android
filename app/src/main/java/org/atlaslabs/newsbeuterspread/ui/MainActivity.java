package org.atlaslabs.newsbeuterspread.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;

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
        binding.button.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_CODE_SETTINGS);
        });

        binding.itemsList.setLayoutManager(new LinearLayoutManager(this));

        // update items list
        if(getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).contains(PREFERENCE_BASE_URL.name())) {
            baseURL = getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE).getString(PREFERENCE_BASE_URL.name(), null);
            updateAPI();
        }
    }

    private void updateAPI(){
        api = RestUtil.createAPI(baseURL);
        api.getUnread()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> loadItems(response.items));
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

    @Nullable
    public NewsbeuterSpreadAPI getAPI(){
        return api;
    }

    private void loadItems(List<Item> items) {
        binding.itemsList.setAdapter(new ItemAdapter(this, new ArrayList<>(items)));
    }
}
