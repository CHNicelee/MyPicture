package com.ice.picture;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ice.picture.adapter.CommentAdapter;
import com.ice.picture.adapter.ViewHolder;
import com.ice.picture.bean.Item;
import com.ice.picture.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 1/19/2017.
 */

public class ChooseActivity extends BaseActivity {

    private String tag2;
    private String tag1;
    private int pn;
    private EditText et_np;
    private EditText et_tag;
    private Button btn_commit;
    private RecyclerView recyclerView;
    private List<String> childList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_activity);
        tag1 = getIntent().getStringExtra("tag1");

        if(Util.itemList!=null)
        for (Item item : Util.itemList) {
            if(item.type.equals(tag1)){
                childList.add(item.item);
            }
        }
        init();
    }

    private int lastSelectedItem=-1;

    CardView lastView;
    private void init() {
        et_np = f(R.id.editText_np);
        et_tag = f(R.id.editText_childType);
        btn_commit = f(R.id.button);
        recyclerView = f(R.id.recyclerView);

        RecyclerView.LayoutManager lm = new GridLayoutManager(this,3);
        recyclerView.setAdapter(new CommentAdapter<String>(this,R.layout.choose_item,childList) {

            @Override
            public void convert(final ViewHolder viewHolder, final String data) {
                viewHolder.setText(R.id.childType,data);
                viewHolder.setOnClickListener(R.id.childType, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(lastView!=null){
//                            CardView cardView = (CardView) recyclerView.getChildAt(lastSelectedItem).findViewById(R.id.cardView);
//                            cardView.setBackgroundColor(Color.WHITE);//恢复上次点击的颜色
                            lastView.setBackgroundColor(Color.WHITE);//恢复上次点击的颜色
//                            lastSelectedItem = recyclerView.indexOfChild(viewHolder.getView(R.id.cardView));//保存点击的item下标
                            lastView = viewHolder.getView(R.id.cardView);
                        }else {
                            lastView = viewHolder.getView(R.id.cardView);
                        }
                        viewHolder.getView(R.id.cardView).setBackgroundColor(Color.LTGRAY);//设置点击的点色为灰色
                        et_tag.setText(data);
                    }
                });
            }
        });
        recyclerView.setLayoutManager(lm);

        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_np.getText().toString().equals("")){
                    pn = 0;
                }else {
                    pn = Integer.parseInt(et_np.getText().toString());
                }

                if(et_tag.getText().toString().equals("")){
                    tag2 = "全部";
                }else {
                    tag2 = et_tag.getText().toString();
                }
                Intent intent = new Intent();
                intent.putExtra("tag2",tag2);
                intent.putExtra("pn",pn);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }


}
