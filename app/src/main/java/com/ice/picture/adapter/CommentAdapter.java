package com.ice.picture.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by asd on 1/13/2017.
 */

public abstract class CommentAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    protected Context mContext;
    protected int mLayoutId;
    protected List<T> mDatas;
    public CommentAdapter(Context context,int layoutId, List<T> datas){
        mContext = context;
        mLayoutId = layoutId;
        mDatas =datas;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ViewHolder.get(mContext,parent,mLayoutId);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        convert(holder,mDatas.get(position));
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract void convert(ViewHolder viewHolder,T data);
}
