package com.ice.picture.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.ice.picture.bean.Item;
import com.ice.picture.util.Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by asd on 1/19/2017.
 */

@SuppressLint("ValidFragment")
public class MainFragment extends BaseFragment {

    private String tag1 = "美女";
    private String tag2 = "全部";
    private ArrayList<String> itemList = new ArrayList<>();//标签
    private int pn = 0;//跳过`
    private int num = 40;//每页数量

    private Context context;

    @SuppressLint("ValidFragment")
    public MainFragment(Context context, String tag1, String tag2, int pn) {
        super(context);
        this.context = context;
        Util.connect(context);
        imageList.clear();//清空列表
        this.tag1 = tag1;
        super.tag2 = tag2;
        this.tag2 = tag2;
        this.pn = pn;
    }


    @Override
    public void setImageList() {
        addImages(getUrl(tag1, tag2, pn, num));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int num = 10;

            public boolean isSlideToBottom(RecyclerView recyclerView) {
                if (recyclerView == null) return false;
                if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                        >= recyclerView.computeVerticalScrollRange() - 500)
                    return true;
                return false;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(recyclerView) && imageList.size() > 0) {
                    //滑到底了   刷新图片
                    pn += num;
                    addImages(getUrl(tag1, tag2, pn, num));
                    Log.d("ICE", "onScrolled");
//                    addIntoAdapter(adapterimageList.size(),10);

                }
            }
        });

    }


    private String getUrl(String tag1, String tag2, int pn, int num) {
        return "http://image.baidu.com/channel/listjson?pn=" + pn + "&rn=" + num + "&tag1=" + tag1 + "&tag2=" + tag2 + "&ie=utf8";
    }
 /*   private void initRecyclerView() {

        // mAdapter = new MyRecyclerViewAdapter(this,imageList);
        recyclerView = f(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //    recyclerView.setAdapter(mAdapter);

        recyclerView.setAdapter(new CommentAdapter<Image>(context, R.layout.recycler_item, imageList) {
            @Override
            public void convert(ViewHolder viewHolder, final Image data) {
                final NetworkImageView img = viewHolder.getView(R.id.image);
                img.setDefaultImageResId(R.mipmap.ic_launcher);
                img.setErrorImageResId(R.mipmap.ic_launcher);
                img.setImageUrl(data.thumbnail_url,imageLoader);
                img.setOnClickListener(new View.OnClickListener() {
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
                CardView cardView = viewHolder.getView(R.id.cardview);
                StaggeredGridLayoutManager.LayoutParams p  = (StaggeredGridLayoutManager.LayoutParams) cardView.getLayoutParams();
                p.height = px2dip(data.height* 2);
                cardView.setLayoutParams(p);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int num = 10;
            public  boolean isSlideToBottom(RecyclerView recyclerView) {
                if (recyclerView == null) return false;
                if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                        >= recyclerView.computeVerticalScrollRange()-500)
                    return true;
                return false;
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(isSlideToBottom(recyclerView) && imageList.size()>0){
                    //滑到底了   刷新图片
                    pn += num;
                    addImages(getUrl(tag1,tag2,pn,num));
                    Log.d("ICE","onScrolled");
//                    addIntoAdapter(adapterimageList.size(),10);

                }
            }
        });

    }*/

    int time = 5;

    private void addImages(String url) {
        Log.d("ICE", url);
        /*StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                imageList.addAll(Util.getImages(response));

                if (imageList.size() == 0 && time > 0) {
                    if(time == 5)
                        show("当前类别无查询结果，已经更改为全部");
                    tag2 = "全部";
                    time --;
                    addImages(getUrl(tag1,"全部",pn,num));
                    return;
                }
                progressBar.setVisibility(View.GONE);

                recyclerView.getAdapter().notifyDataSetChanged();


                //添加到服务器
                if(!tag2.equals("全部") && !Util.isContainItem(tag2)){
                    BmobQuery<Item> query = new BmobQuery<>();
                    query.findObjects(new FindListener<Item>() {
                        @Override
                        public void done(List<Item> list, BmobException e) {
                            if(e==null){
                                //请求成功
                                Util.itemList.clear();
                                Util.itemList.addAll(list);

                                if(!Util.isContainItem(tag2)){
                                    Item item = new Item();
                                    item.type = tag1;
                                    item.item = tag2;
                                    Util.itemList.add(item);
                                    item.save(new SaveListener<String>() {
                                        @Override
                                        public void done(String s, BmobException e) {

                                        }
                                    });
                                }
                            }else {

                            }
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(System.currentTimeMillis() - lastTime <5000){
                    show("请求超时，请检查网络");
                    lastTime = System.currentTimeMillis();
                }
            }
        });
        *//*request.setRetryPolicy(new DefaultRetryPolicy(3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*//*
        queue.add(request);
*/
        new loadTask().execute(url);


    }


    class loadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpClient httpClient = new DefaultHttpClient();
            // 指定访问的服务器地址是电脑本机
            HttpGet httpGet = new HttpGet(params[0]);
            HttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpGet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 请求和响应都成功了
                HttpEntity entity = httpResponse.getEntity();
                //设置字符编码 utf-8 gb2312是中文编码
                try {
                    String response = EntityUtils.toString(entity, "gb2312");
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;

        }
        @Override
        protected void onPostExecute(String response) {
            if(TextUtils.isEmpty(response)){
                show("查询失败，请检查网络");
            }
            imageList.addAll(Util.getImages(response));

            if (imageList.size() == 0 && time > 0) {
                if (time == 5)
                    show("当前类别无查询结果，已经更改为全部");
                tag2 = "全部";
                time--;
                addImages(getUrl(tag1, "全部", pn, num));
                return;
            }
            progressBar.setVisibility(View.GONE);

            recyclerView.getAdapter().notifyDataSetChanged();


            //添加到服务器
            if (!tag2.equals("全部") && !Util.isContainItem(tag2)) {
                BmobQuery<Item> query = new BmobQuery<>();
                query.findObjects(new FindListener<Item>() {
                    @Override
                    public void done(List<Item> list, BmobException e) {
                        if (e == null) {
                            //请求成功
                            Util.itemList.clear();
                            Util.itemList.addAll(list);

                            if (!Util.isContainItem(tag2)) {
                                Item item = new Item();
                                item.type = tag1;
                                item.item = tag2;
                                Util.itemList.add(item);
                                item.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {

                                    }
                                });
                            }
                        } else {

                        }
                    }
                });
            }
        }


    }
}
