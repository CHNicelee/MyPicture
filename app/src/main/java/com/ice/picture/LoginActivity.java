package com.ice.picture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ice.picture.bean.User;
import com.ice.picture.util.Util;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.ice.picture.R.id.autoLogin;

/**
 * Created by asd on 1/19/2017.
 */

public class LoginActivity extends BaseActivity {

    private static final String TAG = "ICE";
    private CheckBox checkBox;
    private EditText et_username,et_password;
    private static final String APP_ID = "1105888885";//官方获取的APPID
    private Tencent mTencent;
    private UserInfo mUserInfo;
    private BaseUiListener mIUiListener;
    RelativeLayout container;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        container = f(R.id.container);
        ImageView imageView = f(R.id.imageView);
        try {
            getBitmapForImgResourse(this,R.drawable.bg,imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //传入参数APPID和全局Context上下文
        mTencent = Tencent.createInstance(APP_ID,this.getApplicationContext());
        Util.connect(this);

        Toolbar toolbar = f(R.id.toolbar);
        setSupportActionBar(toolbar);

        et_password = f(R.id.input_password);
        et_username = f(R.id.input_username);
        checkBox = f(autoLogin);

    }

    //qq登录
    public void buttonLogin(View v){
        /**通过这句代码，SDK实现了QQ的登录，这个方法有三个参数，第一个参数是context上下文，第二个参数SCOPO 是一个String类型的字符串，表示一些权限
         官方文档中的说明：应用需要获得哪些API的权限，由“，”分隔。例如：SCOPE = “get_user_info,add_t”；所有权限用“all”
         第三个参数，是一个事件监听器，IUiListener接口的实例，这里用的是该接口的实现类 */
        mIUiListener = new BaseUiListener();
        //all表示获取所有权限
        mTencent.login(this,"all", mIUiListener);
    }

    //登录
    public void login(final View view){
        ((Button)view).setText("登录中...");
        view.setClickable(false);
        final String username = et_username.getText().toString();
        final String password = et_password.getText().toString();
        if(username.trim().equals("") || password.trim().equals("")){
            show("账号密码不能为空");
            ((Button)view).setText("登录");
            view.setClickable(true);
            return;
        }

        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username",username);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if(e==null){
                    if(list.size()>0){
                        User user = list.get(0);
                        if(user.password.equals(password))
                        {
                            //登录成功
                            if(checkBox.isChecked()) {
                                putString("autoLogin","true");
                                putString("username", username);
                                putString("password", password);
                                putString("name",user.name);
                                putString("userId", "");
                            }else{
                                putString("autoLogin","");
                                putString("username", "");
                                putString("password", "");
                                putString("name","");
                                putString("userId", "");
                            }

                            user = user;
                            Intent intent = new Intent(LoginActivity.this,TypeActivity.class);
                            startActivity(intent);

                            finish();
                        }else {
                            ((Button)view).setText("登录");
                            view.setClickable(true);
                            show("账号密码错误");
                        }
                    }else {
                        ((Button)view).setText("登录");
                        view.setClickable(true);
                        show("账号密码错误");
                    }
                }else {
                    show("连接失败，请检查网络");
                    ((Button)view).setText("登录");
                    view.setClickable(true);
                }
            }
        });


    }
    //注册
    public void register(View view){

        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
//        finish();
    }
    /**
     * 在调用Login的Activity或者Fragment中重写onActivityResult方法
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.REQUEST_LOGIN){
            Tencent.onActivityResultData(requestCode,resultCode,data,mIUiListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 自定义监听器实现IUiListener接口后，需要实现的3个方法
     * onComplete完成 onError错误 onCancel取消
     */
    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            final User user = new User();
//            show("授权成功");
            Log.e(TAG, "response:" + response);
            JSONObject obj = (JSONObject) response;
            try {
                final String openID = obj.getString("openid");
                String accessToken = obj.getString("access_token");
                String expires = obj.getString("expires_in");
//                mTencent.setOpenId(openID);
//                mTencent.setAccessToken(accessToken,expires);
//                QQToken qqToken = mTencent.getQQToken();
//                mUserInfo = new UserInfo(getApplicationContext(),qqToken);
                Util.user.userId = openID;
                user.userId = openID;
                Intent intent = new Intent(LoginActivity.this,TypeActivity.class);
                startActivity(intent);

                //查找是否已经有该用户
                BmobQuery<User> query = new BmobQuery<User>();
                query.addWhereEqualTo("userId",openID );

                putString("autoLogin","true");
                putString("userId",openID);

                query.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        //存在该用户 登录成功
                        if(e==null && list.size()>0){
                            Util.user = list.get(0);
                            finish();

                        }else if(e==null){
                            //添加新用户
                            user.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if(e==null){
                                        user.setObjectId(s);
                                        Util.user = user;
                                    }
                                    finish();
                                }
                            });
                        }
                    }
                });

               /*mUserInfo.getUserInfo(new IUiListener() {
                    @Override
                    public void onComplete(Object response) {
                        Log.e("ICE",response.toString());
                        JSONObject jsonObject = (JSONObject) response;
                        try {

                            final String name = jsonObject.getString("nickname");
                            final User user = new User();
                            user.name = name;
                            user.userId = openID;
                            Util.user.name = name;
                            Util.user.userId = openID;

                            Intent intent = new Intent(LoginActivity.this,TypeActivity.class);
                            startActivity(intent);


                            //查找是否已经有该用户
                            BmobQuery<User> query = new BmobQuery<User>();
                            query.addWhereEqualTo("userId",openID );

                            putString("autoLogin","true");
                            putString("userId",openID);
                            putString("name",name);

                            query.findObjects(new FindListener<User>() {
                                @Override
                                public void done(List<User> list, BmobException e) {
                                    //存在该用户 登录成功
                                    if(e==null && list.size()>0){
                                        Util.user = list.get(0);

                                        Intent intent = new Intent(LoginActivity.this,TypeActivity.class);
                                        startActivity(intent);
                                        finish();

                                    }else if(e==null){
                                        //添加新用户
                                        user.save(new SaveListener<String>() {
                                            @Override
                                            public void done(String s, BmobException e) {
                                                if(e==null){
                                                    Util.user = user;
                                                    Intent intent = new Intent(LoginActivity.this,TypeActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }else {
                                                    show("登录失败");
                                                }
                                            }
                                        });
                                    }else {
                                        show("登录失败，请检查网络");
                                    }
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        Log.e(TAG,"登录失败"+uiError.toString());
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG,"登录取消");
                    }
                });*/
        }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onError(UiError uiError) {
            show("授权失败");
        }

        @Override
        public void onCancel() {
            show("授权取消");
        }

}}

