package com.ice.picture;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ice.picture.bean.Advise;
import com.ice.picture.util.Util;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by asd on 1/20/2017.
 */

public class AboutActivity extends BaseActivity {

    EditText editText;
    TextView tv_version;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        editText=f(R.id.et_advise);
        tv_version = f(R.id.version);


        //获得版本号  是一个int类型的数据
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo("com.ice.picture",
                    PackageManager.GET_CONFIGURATIONS);
            final int versionCode =info.versionCode;
            tv_version.setText("版本号"+versionCode+".0");

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void commitAdvise(final View v){
        v.setClickable(false);
        String content = editText.getText().toString();

        if(TextUtils.isEmpty(content)){
            show("对不起，建议不能为空");
            return;
        }
        Advise advise = new Advise();
        advise.content = content;
        advise.name = Util.user.name;
        advise.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e!=null){
                    show("保存失败");
                }else {
                    editText.setText("");
                    show("提交成功,感谢您的建议");
                }
                v.setClickable(true);
            }

        });

    }

}
