package com.ice.picture;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ice.picture.adapter.CommentAdapter;
import com.ice.picture.adapter.ViewHolder;
import com.ice.picture.bean.Type;
import com.ice.picture.bean.User;
import com.ice.picture.bean.UserCollection;
import com.ice.picture.fragment.CollectionFragment;
import com.ice.picture.fragment.HotFragment;
import com.ice.picture.fragment.MainFragment;
import com.ice.picture.util.Util;

import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    private String tag1 = "美女";
    private String tag2 = "全部";
    private ArrayList<String> itemList = new ArrayList<>();//标签
    private int pn=0;//跳过
    private int num = 40;//每页数量

    private MainFragment mainFragment;
    private FragmentTransaction transaction;
    private RecyclerView drawerRv;
    private DrawerLayout drawer;

    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolBar();


        Util.connect(this);
        tag1 = getIntent().getStringExtra("tag1");

        //检查网络
        if(!netWorkUtil.isNetWorkAvailable()){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("网络错误")
                    .setMessage("当前无法连接网络，请连接网络后重试");
            builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(netWorkUtil.isNetWorkAvailable()){
                        initFragment();
                        return;
                    }else {
                        builder.show();
                    }
                }
            });
            builder.show();

        }
        initFragment();
        getSharedPreferences("config",MODE_PRIVATE).edit().putBoolean("firstTime",true).commit();

        initDrawer();

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        name= (TextView) headerLayout.findViewById(R.id.name);
        name.setText(Util.user.name);
        TextView logout = (TextView) headerLayout.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true)
                        .setTitle("提示")
                        .setMessage("请确保您记得密码，除非您是用QQ登录的，否则忘记密码后无法找回该账号")
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(intent);
                                Util.user = new User();
                                Util.collectionList = new ArrayList<UserCollection>();
                                putString("autoLogin","false");
                                finish();

                            }
                        }).setNegativeButton("取消",null).show();
            }
        });
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerRv = f(R.id.drawer_recyclerView);
        drawer = f(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }


    /**
     * 热点图片
     * @param v
     */
    public void showHot(View v){
        HotFragment hotFragment = new HotFragment(this);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout,hotFragment);
        transaction.commit();

        drawer.closeDrawer(GravityCompat.START);
    }

    public void showCollect(View v){
        CollectionFragment hotFragment = new CollectionFragment(this);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout,hotFragment);
        transaction.commit();

        drawer.closeDrawer(GravityCompat.START);
    }



    private void initDrawer() {

        drawerRv.setAdapter(new CommentAdapter<Type>(this,R.layout.drawer_rv_item,Util.typeList) {
            @Override
            public void convert(ViewHolder viewHolder, final Type data) {
                viewHolder.setText(R.id.textView,data.type);
                viewHolder.setOnClickListener(R.id.textView, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tag1 = data.type;
                        tag2 = "全部";
                        mainFragment = new MainFragment(MainActivity.this,tag1,tag2,0);
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frameLayout,mainFragment);
                        transaction.commit();

                        drawer.closeDrawer(GravityCompat.START);
                    }
                });
            }
        });

        drawerRv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }

    private void initFragment(){
        mainFragment = new MainFragment(this,tag1,tag2,0);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frameLayout, mainFragment);
        transaction.commit();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_type) {
            Intent intent = new Intent(this,ChooseActivity.class);
            intent.putExtra("tag1",tag1);

            startActivityForResult(intent,1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        if(requestCode == 1) {
            Log.d("ICE", "onActivityResult");
            //选择了新的item  刷新当前
            tag2 = data.getStringExtra("tag2");
            pn = data.getIntExtra("pn", 0);
            mainFragment = new MainFragment(this,tag1,tag2,pn);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout,mainFragment);
            transaction.commit();

        }

    }


    long lastClick = 0;
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }


    }

    public void toAboutUs(View v){
        Intent intent = new Intent(this,AboutActivity.class);
        startActivity(intent);
    }
}
