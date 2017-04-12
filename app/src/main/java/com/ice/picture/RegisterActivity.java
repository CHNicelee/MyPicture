package com.ice.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.ice.picture.bean.User;
import com.ice.picture.util.Util;

import java.io.IOException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by asd on 1/19/2017.
 */

public class RegisterActivity extends BaseActivity {

    private EditText et_username,et_password,et_name;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        try {
            getBitmapForImgResourse(this,R.drawable.bg, (ImageView) f(R.id.imageView));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toolbar toolbar = f(R.id.toolbar);
        setSupportActionBar(toolbar);

        et_username = f(R.id.input_username);
        et_password = f(R.id.input_password);
        et_name = f(R.id.input_name);

        Util.connect(this);


    }

    public void register(final View view){
        final String username = et_username.getText().toString();
        final String password = et_password.getText().toString();
        final String name = et_name.getText().toString();

        if(username.trim().equals("") || password.trim().equals("") || name.trim().equals("")){
            show("信息不能为空");
            return;
        }

        view.setClickable(false);
        ((Button)view).setText("注册中...");

        BmobQuery<User> query = new BmobQuery();
        query.addWhereEqualTo("username",username);

        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if(e!=null){
                    show("注册失败");
                    view.setClickable(true);
                    ((Button)view).setText("立即注册");
                    return;
                }else{
                    if(list.size()>0){
                        show("对不起，该账号已经被注册");
                        view.setClickable(true);
                        ((Button)view).setText("立即注册");
                    }else{
                        //可以注册
                        final User user = new User();
                        user.name = name;
                        user.password = password;
                        user.username = username;
                        user.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                show("注册成功");
                                user.setObjectId(s);
                                Util.user = user;
                                Intent intent = new Intent(RegisterActivity.this,TypeActivity.class);
                                startActivity(intent);
                                finish();

                                putString("autoLogin","true");
                                putString("username",username);
                                putString("password",password);
                                putString("name",name);
                                putString("userId","");
                            }
                        });
                    }
                }
            }
        });

    }
/*
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }*/
}
