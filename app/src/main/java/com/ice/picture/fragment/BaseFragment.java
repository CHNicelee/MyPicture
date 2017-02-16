package com.ice.picture.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.ice.picture.BitmapCache;
import com.ice.picture.DetailImageActivity;
import com.ice.picture.R;
import com.ice.picture.adapter.CommentAdapter;
import com.ice.picture.adapter.ViewHolder;
import com.ice.picture.bean.Image;
import com.ice.picture.util.Util;
import com.ice.picture.view.ToastCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 1/19/2017.
 */

public abstract class BaseFragment extends Fragment {

    protected ToastCommon toastCommon = ToastCommon.createToastConfig();

    private ImageLongClickListener listener;

    protected void setImageLongClickListener(ImageLongClickListener longClickListener){
        this.listener = longClickListener;
    }

    protected void show(String text){
        toastCommon.ToastShow(context, (ViewGroup) f(R.id.toast_root),text);
    }


    public BaseFragment(Context context){
        this.context = context;
        imageList= new ArrayList<>();
        Util.connect(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflaterView =  inflater.inflate(R.layout.fragment_main,null);
        init();
        return inflaterView;
    }



    protected View inflaterView;
    protected RecyclerView recyclerView;
    protected RequestQueue queue;
    protected ImageLoader imageLoader;
    protected ProgressBar progressBar;
    protected  List<Image> imageList;
    protected Context context;
    protected String tag2;


    public  int px2dip(float pxValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public <T extends View> T f(int id){
        return (T) inflaterView.findViewById(id);
    }

    private void init(){
        progressBar = f(R.id.progressBar);
        recyclerView = f(R.id.recyclerView);
        queue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(queue,new BitmapCache());
//        addImages(getUrl(tag1,tag2,pn,num));
        initRecyclerView();
        setImageList();
    }

    public abstract void setImageList();



    private void initRecyclerView() {

        recyclerView.setAdapter(new CommentAdapter<Image>(context,R.layout.recycler_item,imageList) {
            @Override
            public void convert(ViewHolder viewHolder, final Image data) {
                final NetworkImageView imageView = viewHolder.getView(R.id.image);
                imageView.setDefaultImageResId(R.drawable.default_pic);
                imageView.setErrorImageResId(R.drawable.error);
                final CardView cardView = viewHolder.getView(R.id.cardview);

                if(listener!=null)
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        listener.onLongClick(cardView,data);
                        return true;
                    }
                });

                if(data.width>300)
                    imageView.setImageUrl(data.thumbnail_url,imageLoader);
                else {
                    imageView.setImageUrl(data.image_url,imageLoader);
                }

                StaggeredGridLayoutManager.LayoutParams p  = (StaggeredGridLayoutManager.LayoutParams) cardView.getLayoutParams();
                p.height = px2dip(data.height* 2);
                cardView.setLayoutParams(p);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,DetailImageActivity.class);
                        intent.putExtra("url",data.image_url);
                        intent.putExtra("position",imageList.indexOf(data));
                        intent.putExtra("childType",tag2);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("imageList", (Serializable) imageList);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

    }

    protected interface ImageLongClickListener{
        void onLongClick(CardView cardView, Image image);
    }

}

