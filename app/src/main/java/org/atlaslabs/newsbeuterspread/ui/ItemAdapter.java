package org.atlaslabs.newsbeuterspread.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.atlaslabs.newsbeuterspread.R;
import org.atlaslabs.newsbeuterspread.databinding.ViewItemBinding;
import org.atlaslabs.newsbeuterspread.models.Item;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private WeakReference<MainActivity> activity;
    private List<Item> items;

    public ItemAdapter(MainActivity activity, List<Item> items) {
        this.activity = new WeakReference<>(activity);
        this.items = items;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.binding.itemAuthor.setText(item.author);
        holder.binding.itemContent.setText(item.content);
        holder.binding.itemDate.setText(item.pub_date);
        holder.binding.itemTitleView.setText(item.title);
        holder.binding.itemLinkButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            if(activity.get() != null)
                activity.get().startActivity(browserIntent);
        });
        holder.binding.itemReadButton.setOnClickListener(v -> {
            if(activity.get() != null)
                activity.get().getAPI().markRead(item.id)
                        .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> remove(item));
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
        items.remove(index);
        notifyItemRemoved(index);
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
