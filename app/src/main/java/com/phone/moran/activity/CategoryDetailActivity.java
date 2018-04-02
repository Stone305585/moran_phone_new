package com.phone.moran.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.phone.moran.R;
import com.phone.moran.adapter.BaseRecyclerAdapter;
import com.phone.moran.adapter.FilterRecyclerAdapter;
import com.phone.moran.adapter.ImageGridRecyclerAdapter;
import com.phone.moran.config.Constant;
import com.phone.moran.model.Paint;
import com.phone.moran.model.Picture;
import com.phone.moran.presenter.implPresenter.CategoryDetailActivityImpl;
import com.phone.moran.presenter.implView.ICategoryDetailActivity;
import com.phone.moran.tools.DensityUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryDetailActivity extends BaseActivity implements View.OnClickListener, ICategoryDetailActivity {

    public static final String TYPEID = "type_id";

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
    @BindView(R.id.filter_btn)
    LinearLayout filterBtn;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.category_tv)
    TextView categoryTv;

    private int typeId = 11;
    private ImageGridRecyclerAdapter picAdapter;
    private List<Picture> pictureList = new ArrayList<>();
    private CategoryDetailActivityImpl caImpl;

    //--------------pop Window-------------
    private View popView;
    private RecyclerView popRecycler;
    private FilterRecyclerAdapter filterRecyclerAdapter;
    private List<Paint> filterPaint = new ArrayList<>();
    private int paintId;

    private int last_id;

    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        ButterKnife.bind(this);

        typeId = Integer.valueOf(getIntent().getStringExtra(TYPEID));
        paintId = getIntent().getIntExtra(Constant.PAINT_ID, 2);

        caImpl = new CategoryDetailActivityImpl(this, token, this);


        initView();

        initPopWin();

        setListener();

        initDataSource();
    }

    @Override
    protected void initView() {
        super.initView();

        title.setText("分类");
        categoryTv.setText(getIntent().getStringExtra(Constant.TITLE));
        picAdapter = new ImageGridRecyclerAdapter(this, pictureList);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setLayoutManager(new GridLayoutManager(this, 3));
        recycler.setAdapter(picAdapter);
    }

    public void initPopWin() {
        popView = LayoutInflater.from(this).inflate(R.layout.filter_window, null);
        popupWindow = new PopupWindow(popView, DensityUtils.dip2px(230), DensityUtils.dip2px(130), true);
        popupWindow.setContentView(popView);
        popupWindow.setAnimationStyle(R.style.filterpopwindow_anim_style);

        // 实例化一个ColorDrawable颜色为半透明
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.corner_bg_white_pop));

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

        filterRecyclerAdapter = new FilterRecyclerAdapter(this, filterPaint, false);

        popRecycler = (RecyclerView) (popView.findViewById(R.id.filter_recycler));
        popRecycler.setItemAnimator(new DefaultItemAnimator());
        popRecycler.setLayoutManager(new LinearLayoutManager(this));
        popRecycler.setAdapter(filterRecyclerAdapter);

    }

    @Override
    protected void setListener() {
        super.setListener();
        filterBtn.setOnClickListener(this);
        backTitle.setOnClickListener(this);

        filterRecyclerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                for (Paint item : filterPaint) {
                    item.setSelected(false);
                }
                Paint paint = (Paint) model;
                paint.setSelected(true);

                last_id = 0;

                filterRecyclerAdapter.notifyDataSetChanged();

                pictureList.clear();

                caImpl.updateMain(paint.getPaint_id(), last_id);

                categoryTv.setText(paint.getPaint_name());

                popupWindow.dismiss();
            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });

        picAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                /*Picture picture = (Picture) model;
                Intent intent = new Intent(CategoryDetailActivity.this, PictureActivity.class);
                intent.putExtra(Constant.PICTURE_ID, picture.getPicture_id());
                startActivity(intent);*/

                Intent intent = new Intent(CategoryDetailActivity.this, PlayPictureActivity.class);
                intent.putExtra(Constant.PLAY_FLAG, PlayPictureActivity.PAINT);
                intent.putExtra(Constant.PAINT, paint);
                startActivity(intent);

            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });

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

                    caImpl.updateMain(paintId, last_id);
                    //重置last_id  防止多次请求  请求成功后  会把最新的last_id重新赋值
                    last_id = 0;
                }
            }
        });
    }

    @Override
    protected void initDataSource() {
        super.initDataSource();
        if (connected) {
            caImpl.updateFilter(typeId);
            caImpl.updateMain(paintId, last_id);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.filter_btn:
                popupWindow.showAtLocation(filterBtn, Gravity.TOP, 0, DensityUtils.dip2px(116));
                // 设置popWindow弹出窗体可点击
                popupWindow.setFocusable(true);
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 0.7f;
                getWindow().setAttributes(params);
                break;

            case R.id.back_title:
                finish();
                break;
        }

    }

    @Override
    public void updateMain(Paint paint, List<Picture> paints, int last_id) {
        this.last_id = last_id;
        this.paint = paint;

        pictureList.addAll(paints);
        picAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateFilter(List<Paint> paints) {
        filterPaint.clear();
        filterPaint.addAll(paints);
        filterRecyclerAdapter.notifyDataSetChanged();
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
//        AppUtils.showToast(getApplicationContext(), error);
    }

}
