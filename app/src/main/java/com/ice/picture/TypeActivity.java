package com.ice.picture;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.ice.picture.adapter.CommentAdapter;
import com.ice.picture.adapter.ViewHolder;
import com.ice.picture.bean.Item;
import com.ice.picture.bean.Type;
import com.ice.picture.bean.User;
import com.ice.picture.bean.UserCollection;
import com.ice.picture.util.Util;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.view.View.GONE;

/**
 * Created by asd on 1/19/2017.
 */

public class TypeActivity extends BaseActivity {
    private List<Type> typeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RequestQueue queue;
    private ImageLoader imageLoader;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.type_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Util.connect(this);
        queue = Volley.newRequestQueue(this);
        imageLoader = new ImageLoader(queue,new BitmapCache());

        if(!netWorkUtil.isNetWorkAvailable()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("网络错误")
                    .setMessage("当前无法连接网络，请连接网络后重试");
            builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(netWorkUtil.isNetWorkAvailable()){
                        checkName();
                        return;
                    }else {
                        builder.show();
                    }
                }
            });
            builder.show();

            return;
        }
        //checkName();
        init();
        getDataFromServer();
    }

    private void checkName() {
        progressBar = f(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if(Util.user.name==null || Util.user.name.equals("")){
            final LinearLayout wrapper = f(R.id.wrapper);

            BmobQuery<User> query = new BmobQuery();
            query.addWhereEqualTo("userId",Util.user.userId);
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if(e==null && list.size()>0) Util.user =list.get(0);
                    else {
                        checkName();
                        return;
                    }

                    if(Util.user.name==null || Util.user.name.equals("")){
                        wrapper.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(GONE);
                        final Button button = f(R.id.btn_commit);
                        final EditText editText = f(R.id.et_name);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                button.setText("提交中");
                                button.setClickable(false);
                                String name = editText.getText().toString();
                                if(!name.trim().equals("")){
                                    Util.user.name = name;
                                    Util.user.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if(e==null) {
                                                wrapper.setVisibility(GONE);
                                                progressBar.setVisibility(View.VISIBLE);
                                                init();
                                                getDataFromServer();
                                            }else{
                                                button.setText("提交");
                                                button.setClickable(true);
                                                show("提交失败");
                                            }

                                        }
                                    });
                                }else {
                                    button.setText("提交");
                                    button.setClickable(true);
                                    show("昵称不能为空");
                                }
                            }
                        });
                    }else {
                        wrapper.setVisibility(GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        init();
                        getDataFromServer();
                    }

                }
            });

        }else {
            init();
            getDataFromServer();
        }
    }

    private void init() {
        progressBar = f(R.id.progressBar);
        if(TextUtils.isEmpty(Util.user.userId)){
            Util.userType = Util.NORMAL_USER;
        }else {
            Util.userType = Util.QQ_USER;
        }

        recyclerView = f(R.id.recyclerView);
        recyclerView.setAdapter(new CommentAdapter<Type>(this, R.layout.type_item, typeList) {
            @Override
            public void convert(ViewHolder viewHolder, final Type data) {
                NetworkImageView imageView = viewHolder.getView(R.id.imageView);
                imageView.setDefaultImageResId(R.drawable.default_pic);
                imageView.setImageUrl(data.imageUrl,imageLoader);

                viewHolder.setText(R.id.textView_type,data.type);
                viewHolder.setOnClickListener(R.id.cardView, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TypeActivity.this,MainActivity.class);
                        intent.putExtra("tag1",data.type);
                        startActivity(intent);
                        Util.typeList = typeList;
                    }
                });
            }
        } );
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

    }

    public void getDataFromServer() {
        BmobQuery<Type> query = new BmobQuery<>();
        query.findObjects(new FindListener<Type>() {
            @Override
            public void done(List<Type> list, BmobException e) {
                if(e==null){
                    //请求成功
                    typeList.addAll(list);
                    progressBar.setVisibility(GONE);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }else {
                    show("网络似乎有点问题...");
                    Log.e("ICE",e.getMessage());
                }
            }
        });

        BmobQuery<Item> queryItem = new BmobQuery<>();
        queryItem.findObjects(new FindListener<Item>() {
            @Override
            public void done(List<Item> list, BmobException e) {
                if(e==null)
                  Util.itemList = list;
                else{
                    show("网络似乎有点问题...");
                    Log.e("ICE",e.getMessage());
                }
            }
        });

        getUserCollection();


    }

    int time =5;

    public void getUserCollection() {

        final BmobQuery<UserCollection> userCollectionBmobQuery = new BmobQuery<>();
        if(TextUtils.isEmpty(Util.user.userId)){
            userCollectionBmobQuery.addWhereEqualTo("username",Util.user.username);
        }else {
            userCollectionBmobQuery.addWhereEqualTo("userId",Util.user.userId);
        }
        userCollectionBmobQuery.findObjects(new FindListener<UserCollection>() {
            @Override
            public void done(List<UserCollection> list, BmobException e) {
                if(e==null){
                    Util.collectionList = list;
                }else {
                    if(time > 0)
                        getUserCollection();
                    time --;

                }
            }
        });
    }
    long lastClick = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.others,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.close:
                finish();
                break;
            case R.id.logout:
                finish();
                startActivity(new Intent(this,LoginActivity.class));
                Util.user = new User();
                Util.collectionList = new ArrayList<>();
                putString("autoLogin","false");

                break;
            case R.id.aboutUs:
                startActivity(new Intent(this,AboutActivity.class));
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if( System.currentTimeMillis()- lastClick <1000){
            finish();
            super.onBackPressed();
        }else {
            lastClick =System.currentTimeMillis();
            show("再按一次退出程序");
        }
    }
}
