package org.atlaslabs.newsbeuterspread.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ViewItemBinding;
import org.atlaslabs.newsbeuterspread.models.Item;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_JS_ENABLE;
import static org.atlaslabs.newsbeuterspread.ui.SettingsActivity.Setting.PREFERENCE_NAME;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private static final String TAG = ItemAdapter.class.getSimpleName();
    private final WeakReference<MainActivity> activity;
    private final List<Item> items;
    private final boolean jsEnabled;

    public ItemAdapter(MainActivity activity, @NonNull List<Item> items) {
        this.activity = new WeakReference<>(activity);
        this.items = items;
        jsEnabled = activity.getSharedPreferences(PREFERENCE_NAME.name(), Context.MODE_PRIVATE)
                .getBoolean(PREFERENCE_JS_ENABLE.name(), false);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.binding.itemAuthor.setText(item.author);
        holder.binding.itemContent.setVisibility(TextUtils.isEmpty(item.content) ? View.GONE : View.VISIBLE);
        holder.binding.itemContent.loadData(item.content, "text/html", "utf-8");
        holder.binding.itemContent.getSettings().setJavaScriptEnabled(jsEnabled);
        holder.binding.itemDate.setText(item.pub_date);
        holder.binding.itemTitleView.setText(item.title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            holder.binding.itemLinkButton.setTooltipText(item.url);
        holder.binding.itemLinkButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            if(activity.get() != null)
                activity.get().startActivity(browserIntent);
        });
        holder.binding.itemReadButton.setOnClickListener(v -> {
            if(activity.get() != null && activity.get().getAPI() != null)
                activity.get().getAPI().markRead(item.id)
                        .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if(activity.get() == null)
                                return;
                            remove(item);
                            if(items.isEmpty())
                                activity.get().updateAPI();
                        }, e -> {
                            if(activity.get() != null) {
                                String text = "An error has occurred marking as read: " + e;
                                Toast.makeText(activity.get(), text, Toast.LENGTH_SHORT).show();
                            }
                        });
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.view_item, parent, false);
        return new ViewHolder(binding);
    }

    private void remove(Item item){
        int index = items.indexOf(item);
        if(index != -1) {
            items.remove(index);
            notifyItemRemoved(index);
        }else
            Log.w(TAG, "Attempting to remove item not in list: " + item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewItemBinding binding;

        ViewHolder(ViewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
