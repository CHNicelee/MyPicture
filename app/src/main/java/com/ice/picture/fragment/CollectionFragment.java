package com.ice.picture.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.ice.picture.R;
import com.ice.picture.bean.UserCollection;
import com.ice.picture.util.Util;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.view.View.GONE;
import static com.ice.picture.util.Util.QQ_USER;

/**
 * Created by asd on 1/20/2017.
 */

@SuppressLint("ValidFragment")
public class CollectionFragment extends BaseFragment {
    private PopupWindow pop;
    private View mainLayout;//这个Activity的界面

    public CollectionFragment(Context context) {
        super(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_main,null);
        return super.onCreateView(inflater, container, savedInstanceState);
    }



    @Override
    public void setImageList() {
        getDataFromServer();
    }

    public List<UserCollection> collectionList;

    public void getDataFromServer() {
        BmobQuery<UserCollection> query = new BmobQuery<>();
        if(Util.userType == QQ_USER){
            query.addWhereEqualTo("userId",Util.user.userId);
        }else {
            query.addWhereEqualTo("username",Util.user.username);
        }
        query.findObjects(new FindListener<UserCollection>() {
            @Override
            public void done(List<UserCollection> list, BmobException e) {
                if(e==null){
                    collectionList = list;
                    Util.collectionList = list;
                    for (int i = 0; i < list.size(); i++) {
                        imageList.add(list.get(i));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                    progressBar.setVisibility(GONE);
                }else {
                    Log.e("ICE",e.getMessage());
                    show("网络有误 请重试");
                }
            }
        });

    }
}
