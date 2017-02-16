package com.ice.picture.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.ice.picture.bean.ImageHot;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static android.view.View.GONE;

/**
 * Created by asd on 1/20/2017.
 */

@SuppressLint("ValidFragment")
public class HotFragment extends BaseFragment {

    private String tag2 = "全部";

    private Context context;
    @SuppressLint("ValidFragment")
    public HotFragment(Context context){
        super(context);
        this.context = context;


    }
   /* @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main,null);
        super.inflaterView = view;
        initData();
        return view;
    }
*/
    @Override
    public void setImageList() {
        getDataFromServer();
    }
/*

    private void initData() {
        progressBar = f(R.id.progressBar);
        recyclerView = f(R.id.recyclerView);
        queue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(queue,new BitmapCache());
    }
*/

    public  List<ImageHot> imageHotList;

    public void getDataFromServer() {
        BmobQuery<ImageHot> query = new BmobQuery<>();
        query.findObjects(new FindListener<ImageHot>() {
            @Override
            public void done(List<ImageHot> list, BmobException e) {
                if(e==null){
                    imageHotList = list;
                    for (int i = 0; i < list.size(); i++) {
                        imageList.add(list.get(i));
                    }
                    recyclerView.getAdapter().notifyDataSetChanged();
                    progressBar.setVisibility(GONE);
                }else {
                    Log.e("ICE",e.getMessage());
                    show("网络有误");
                }
            }
        });

    }

/*    private void initRecyclerView() {

        recyclerView.setAdapter(new CommentAdapter<ImageHot>(context,R.layout.recycler_item,imageHotList) {
            @Override
            public void convert(ViewHolder viewHolder, final ImageHot data) {
                NetworkImageView imageView = viewHolder.getView(R.id.image);
                imageView.setDefaultImageResId(R.layout.detail_image);
                imageView.setImageUrl(data.image_url,imageLoader);
                imageView.setErrorImageResId(R.mipmap.ic_launcher);
                CardView cardView = viewHolder.getView(R.id.cardview);
                StaggeredGridLayoutManager.LayoutParams p  = (StaggeredGridLayoutManager.LayoutParams) cardView.getLayoutParams();
                p.height = px2dip(data.height* 2);
                cardView.setLayoutParams(p);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context,DetailImageActivity.class);
                        intent.putExtra("url",data.image_url);
                        intent.putExtra("position",imageHotList.indexOf(data));
                        intent.putExtra("childType",data.childType);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("imageList", (Serializable) imageHotList);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));

    }*/
}
