package com.phone.moran.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phone.moran.R;
import com.phone.moran.adapter.BaseRecyclerAdapter;
import com.phone.moran.adapter.ImageGridRecyclerAdapter;
import com.phone.moran.config.Constant;
import com.phone.moran.model.LocalMoods;
import com.phone.moran.model.Mood;
import com.phone.moran.model.Paint;
import com.phone.moran.model.Picture;
import com.phone.moran.presenter.implPresenter.PaintActivityImpl;
import com.phone.moran.presenter.implView.IPaintActivity;
import com.phone.moran.tools.AppUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoodPicsActivity extends BaseActivity implements View.OnClickListener, IPaintActivity {

    @BindView(R.id.back_title)
    ImageView backTitle;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right_text_btn)
    TextView rightTextBtn;
    @BindView(R.id.right_image_btn1)
    ImageView rightImageBtn1;
    @BindView(R.id.right_image_btn2)
    ImageView rightImageBtn2;
    @BindView(R.id.right_image_btn3)
    ImageView rightImageBtn3;
    @BindView(R.id.rl_title)
    LinearLayout rlTitle;
    @BindView(R.id.toolbar_common)
    Toolbar toolbarCommon;
    @BindView(R.id.title_mood)
    TextView titleMood;
    @BindView(R.id.recycler)
    RecyclerView recycler;

    ImageGridRecyclerAdapter adapter;

    private ArrayList<Picture> pictures = new ArrayList<>();

    Mood mood;
    int flag; //0:用户自定义  1：初始分类
    private int last_id = 0;
    PaintActivityImpl paintImpl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_pics);
        ButterKnife.bind(this);

        mood = (Mood) getIntent().getSerializableExtra(Constant.PAINT);
        flag = getIntent().getIntExtra(Constant.TITLE, 0);
        paintImpl = new PaintActivityImpl(this, token, this);

        initView();

        setListener();
    }

    @Override
    protected void initView() {
        super.initView();

        title.setText(mood.getMood_name());

        titleMood.setText(flag == 0 ? getResources().getString(R.string.user_custom): getResources().getString(R.string.default_category));

        if (flag == 0) {
            LocalMoods localMoods = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MOOD + userId);

            mood = localMoods.getMoodById(mood.getMood_id());

            pictures.addAll(mood.getPictures());

        } else {
            paintImpl.getMoodDetail(mood.getMood_id(), last_id);
        }

        adapter = new ImageGridRecyclerAdapter(this, pictures);

        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        recycler.setAdapter(adapter);
    }


    @Override
    protected void setListener() {
        super.setListener();

        backTitle.setOnClickListener(this);

        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                /*Picture pic = (Picture) model;

                Intent intent = new Intent(MoodPicsActivity.this, PictureActivity.class);
                intent.putExtra(Constant.PICTURE_ID, pic.getPicture_id());
                startActivity(intent);*/

                Paint paint = new Paint();
                paint.setPicture_info(mood.getPictures());
                paint.setPaint_title(mood.getMood_name());

                Intent intent = new Intent(MoodPicsActivity.this, PlayPictureActivity.class);
                intent.putExtra(Constant.PLAY_FLAG, PlayPictureActivity.PAINT);
                intent.putExtra(Constant.PAINT, paint);
                startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });

        if (flag == 1)
            recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();

                    int index = manager.findLastVisibleItemPosition();

                    if (index >= manager.getItemCount() - 1 && last_id != 0) {
                        paintImpl.getPaintDetail(mood.getMood_id(), last_id);
                        last_id = 0;
                    }
                }
            });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_title:
                finish();
                break;
        }
    }

    @Override
    public void showProgressDialog() {
        dialog.show();
    }

    @Override
    public void hidProgressDialog() {
        dialog.hide();
    }

    @Override
    public void showError(String error) {
        dialog.hide();
        AppUtils.showToast(getApplicationContext(), error);
    }

    @Override
    public void updatePaint(Paint paint) {

        last_id = paint.getLast_id();

        pictures.addAll(paint.getPicture_info());

        adapter.notifyDataSetChanged();
    }

    @Override
    public void collectSuccess() {

    }

    @Override
    public void uploadSuccess() {

    }
}
