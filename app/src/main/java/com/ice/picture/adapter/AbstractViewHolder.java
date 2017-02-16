package com.ice.picture.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by asd on 1/9/2017.
 */

public abstract class AbstractViewHolder<T> extends RecyclerView.ViewHolder {
    public AbstractViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindViewHolder(T model);
}
