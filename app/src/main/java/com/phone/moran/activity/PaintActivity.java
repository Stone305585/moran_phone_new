package com.phone.moran.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.phone.moran.HHApplication;
import com.phone.moran.R;
import com.phone.moran.adapter.BaseRecyclerAdapter;
import com.phone.moran.adapter.ImageGridRecyclerAdapter;
import com.phone.moran.config.Constant;
import com.phone.moran.event.AddRecentEvent;
import com.phone.moran.event.CollectEvent;
import com.phone.moran.model.LocalPaints;
import com.phone.moran.model.Paint;
import com.phone.moran.model.Picture;
import com.phone.moran.presenter.implPresenter.PaintActivityImpl;
import com.phone.moran.presenter.implView.IPaintActivity;
import com.phone.moran.tools.AppUtils;
import com.phone.moran.tools.ImageLoader;
import com.phone.moran.tools.SLogger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

import static android.view.View.VISIBLE;

public class PaintActivity extends BaseActivity implements View.OnClickListener, IPaintActivity {

    private static final String COLLECT = "collect";
    private static final String UNCOLLECT = "uncollect";

    public static final String RECOMMEND = "recommend";
    public static final String TODAY = "today";


    @BindView(R.id.blur_bg_yishu)
    LinearLayout blurBgYishu;
    @BindView(R.id.image_cover_gone)
    ImageView imageCoverGone;
    @BindView(R.id.back_title)
    ImageView backTitle;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right_image_btn3)
    ImageView rightImageBtn3;
    @BindView(R.id.rl_title)
    LinearLayout rlTitle;
    @BindView(R.id.image_cover)
    ImageView imageCover;
    @BindView(R.id.num_look)
    TextView numLook;
    @BindView(R.id.num_pieces)
    TextView numPieces;
    @BindView(R.id.title_gallery)
    TextView titleGallery;
    @BindView(R.id.author_gallery)
    TextView authorGallery;
    @BindView(R.id.upload_gallery)
    ImageView uploadGallery;
    @BindView(R.id.collect_gallery)
    ImageView collectGallery;
    @BindView(R.id.share_gallery)
    ImageView shareGallery;
    @BindView(R.id.left_btn_gallery)
    RadioButton leftBtnGallery;
    @BindView(R.id.center_btn_gallery)
    RadioButton centerBtnGallery;
    @BindView(R.id.right_btn_gallery)
    RadioButton rightBtnGallery;
    @BindView(R.id.num_bottom_gallery)
    TextView numBottomGallery;
    @BindView(R.id.paint_recycler)
    RecyclerView paintRecycler;
    @BindView(R.id.detail_gallery)
    TextView detailGallery;
    @BindView(R.id.default_text)
    TextView defaultText;

    private View footerView;


    private Paint paint;
    private List<Picture> picturesHList = new ArrayList<>();
    private List<Picture> picturesSList = new ArrayList<>();
    private List<Picture> picturesHSList = new ArrayList<>();

    private ImageGridRecyclerAdapter imageGridRecyclerAdapter;
    private PaintActivityImpl paintActivityImpl;
    private int paintId;

    private String paintTitle;

    private int last_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String flag = getIntent().getStringExtra(Constant.PAINT);
        if (flag != null && flag.equals(RECOMMEND)) {
            setContentView(R.layout.activity_gallery);
        } else
            setContentView(R.layout.activity_gallery_yishu);
        ButterKnife.bind(this);

        paintId = getIntent().getIntExtra(Constant.PAINT_ID, 0);
        paintTitle = getIntent().getStringExtra(Constant.PAINT_TITLE);

        paintActivityImpl = new PaintActivityImpl(this, token, this);

        initView();
        setListener();
        initDataSource();
    }

    @Override
    protected void initDataSource() {
        super.initDataSource();

        SLogger.d("<<", "title is -->>" + paintTitle);

        if (paintTitle == null) {
            if (connected) {
                paintActivityImpl.getPaintDetail(paintId, last_id);
            }
        } else {
            LocalPaints lp = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
            paint = lp.getPaintByTitle(paintTitle);

            paintId = paint.getPaint_id();
            updatePaint(paint);
        }

    }


    @Override
    protected void initView() {
        super.initView();

        //设置footer
        footerView = LayoutInflater.from(this).inflate(R.layout.footer_recycler, null);

        imageGridRecyclerAdapter = new ImageGridRecyclerAdapter(this, picturesHList);
        paintRecycler.setItemAnimator(new DefaultItemAnimator());
        paintRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        paintRecycler.setAdapter(imageGridRecyclerAdapter);
//        imageGridRecyclerAdapter.setmFooterView(footerView);

        collectGallery.setTag(UNCOLLECT);

    }

    @Override
    protected void setListener() {
        super.setListener();

        backTitle.setOnClickListener(this);
        rightImageBtn3.setOnClickListener(this);

        shareGallery.setOnClickListener(this);
        collectGallery.setOnClickListener(this);
        uploadGallery.setOnClickListener(this);

        leftBtnGallery.setOnClickListener(this);
        centerBtnGallery.setOnClickListener(this);
        rightBtnGallery.setOnClickListener(this);

        titleGallery.setOnClickListener(this);
        imageCover.setOnClickListener(this);

        imageGridRecyclerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                if (!goLogin()) {
                    Intent intent = new Intent(PaintActivity.this, PlayPictureActivity.class);
                    intent.putExtra(Constant.PLAY_FLAG, PlayPictureActivity.PAINT);
                    intent.putExtra(Constant.PAINT, paint);
                    startActivity(intent);
                }

//                Picture picture = (Picture) model;
//                Intent intent = new Intent(PaintActivity.this, PictureActivity.class);
//                intent.putExtra(Constant.PICTURE_ID, picture.getPicture_id());
//                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });

        //非本地画单
        if (paintTitle == null)
            paintRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    GridLayoutManager manager = (GridLayoutManager) paintRecycler.getLayoutManager();

                    int index = manager.findLastVisibleItemPosition();

                    if (index >= manager.getItemCount() - 1 && last_id != 0) {
                        paintActivityImpl.getPaintDetail(paintId, last_id);
                        last_id = 0;
                    }
                }
            });
    }

    protected boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange())
            return true;
        return false;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //横
            case R.id.left_btn_gallery:
                if (picturesHList.size() == 0) {
                    defaultText.setVisibility(VISIBLE);
                    paintRecycler.setVisibility(View.GONE);
                } else {
                    defaultText.setVisibility(View.GONE);
                    paintRecycler.setVisibility(VISIBLE);
                    imageGridRecyclerAdapter.setmData(picturesHList);
                    imageGridRecyclerAdapter.notifyDataSetChanged();
                }
                leftBtnGallery.setChecked(true);
                centerBtnGallery.setChecked(false);
                rightBtnGallery.setChecked(false);

                numBottomGallery.setText(String.valueOf(picturesHList.size()));
                break;
            //横竖
            case R.id.center_btn_gallery:
                if (picturesHSList.size() == 0) {
                    defaultText.setVisibility(VISIBLE);
                    paintRecycler.setVisibility(View.GONE);
                } else {
                    defaultText.setVisibility(View.GONE);
                    paintRecycler.setVisibility(VISIBLE);
                    imageGridRecyclerAdapter.setmData(picturesHSList);
                    imageGridRecyclerAdapter.notifyDataSetChanged();
                }

                centerBtnGallery.setChecked(true);
                leftBtnGallery.setChecked(false);
                rightBtnGallery.setChecked(false);

                numBottomGallery.setText(String.valueOf(picturesHSList.size()));
                break;
            //竖
            case R.id.right_btn_gallery:
                if (picturesSList.size() == 0) {
                    defaultText.setVisibility(VISIBLE);
                    paintRecycler.setVisibility(View.GONE);
                } else {
                    defaultText.setVisibility(View.GONE);
                    paintRecycler.setVisibility(VISIBLE);
                    imageGridRecyclerAdapter.setmData(picturesSList);
                    imageGridRecyclerAdapter.notifyDataSetChanged();
                }

                rightBtnGallery.setChecked(true);
                leftBtnGallery.setChecked(false);
                centerBtnGallery.setChecked(false);

                numBottomGallery.setText(String.valueOf(picturesSList.size()));
                break;

            case R.id.share_gallery:
                if (!goLogin() && paint != null) {
                    Intent intent = new Intent(this, ShareMainActivity.class);
                    intent.putExtra(Constant.PAINT, paint);
                    startActivity(intent);
                }
                break;

            case R.id.upload_gallery:
                if (!goLogin()) {
                    List<Integer> list = new ArrayList<>();
                    for (Picture item : paint.getPicture_info()) {
                        list.add(item.getPicture_id());
                    }
                    paintActivityImpl.upload(list, paint.getPaint_title(), String.valueOf(paint.getPaint_id()));
                }

                break;

            case R.id.collect_gallery:
                if (!goLogin()) {

                    if (paintTitle != null) {
                        //TODO 修改了 可以删除本地画单
//                        collectSuccess();
                    } else
                        paintActivityImpl.collect(paintId);
                }
                break;

            //进入播放页面
            case R.id.right_image_btn3:
                if (goLogin()) {
                    return;
                }
                Intent intent = new Intent(this, PlayPictureActivity.class);
                intent.putExtra(Constant.PLAY_FLAG, PlayPictureActivity.PAINT);
                intent.putExtra(Constant.PAINT, paint);
                startActivity(intent);
                break;

            case R.id.image_cover:
            case R.id.title_gallery:
                if (paintTitle != null) {
                    Intent intent1 = new Intent(PaintActivity.this, EditPaintActivity.class);
                    intent1.putExtra(Constant.PAINT_TITLE, paintTitle);
                    startActivityForResult(intent1, EDITPAINT);
                }
                //TODO 编辑资料
                break;

            case R.id.back_title:
                finish();
                break;

        }

    }

    private static final int EDITPAINT = 110;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == EDITPAINT) {
                if(data != null) {
                    paintTitle = data.getStringExtra(Constant.PAINT_TITLE);
                    initDataSource();
                }

            }
        }
    }

    @Override
    public void collectSuccess() {
        LocalPaints localPaints = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_COLLECT + userId);

        if (collectGallery.getTag() == COLLECT) {
            collectGallery.setImageResource(R.mipmap.collect);
            collectGallery.setTag(UNCOLLECT);

            if (localPaints == null) {
                return;
            }
            for (int i = 0; i < localPaints.getPaints().size(); i++) {
                if (localPaints.getPaints().get(i).getPaint_id() == paintId) {
                    localPaints.getPaints().remove(i);
                    break;
                }
            }
            if (localPaints != null)
                diskLruCacheHelper.put(Constant.LOCAL_COLLECT + userId, localPaints);

        } else {

            collectGallery.setImageResource(R.mipmap.collected);
            collectGallery.setTag(COLLECT);

            LocalPaints localPaints1 = new LocalPaints();
            localPaints1.getPaints().add(paint);
            if (localPaints == null) {
                localPaints = localPaints1;
            } else {
                localPaints.getPaints().add(paint);
            }
        }

        diskLruCacheHelper.put(Constant.LOCAL_COLLECT + userId, localPaints);


        SLogger.d("<<", "collect-->>" + userId + "--->>>" + Constant.LOCAL_COLLECT + userId);


        EventBus.getDefault().post(new CollectEvent());
    }

    @Override
    public void uploadSuccess() {
        AppUtils.showToast(this, getResources().getString(R.string.push_success));
    }

    @Override
    public void updatePaint(Paint paint1) {
        this.paint = paint1;

//        if (paintTitle == null) {
        try {

            title.setText(paintTitle == null ? "艺术先锋" : paintTitle);
            ImageLoader.displayImg(this, paint.getTitle_url() == null ? paint.getPicture_info().get(0).getPicture_url() : paint.getTitle_url(), imageCover);
            ImageLoader.getInstance().setBlurImage(this, paint.getTitle_url() == null ? paint.getPicture_info().get(0).getPicture_url() : paint.getTitle_url(), blurBgYishu, imageCoverGone);
            titleGallery.setText(paint.getPaint_title());

            SLogger.d("<<", "paint detail ----->" + paint.getPaint_detail());
            if (paint.getPaint_detail() != null) {
                detailGallery.setVisibility(VISIBLE);
                detailGallery.setText(paint.getPaint_detail());
            } else {
                detailGallery.setVisibility(View.GONE);
            }
            numPieces.setText(String.valueOf(paint.getPicture_info().size()));
            numLook.setText(String.valueOf(paintTitle == null ? paint.getRead_num() : 0));
            if (paintTitle == null) {
                if (paint.getFlag() == 1) {
                    collectGallery.setImageResource(R.mipmap.collected);
                    collectGallery.setTag(COLLECT);
                } else {
                    collectGallery.setImageResource(R.mipmap.collect);
                    collectGallery.setTag(UNCOLLECT);
                }
            } else {
                collectGallery.setImageResource(R.mipmap.collected);
                collectGallery.setTag(COLLECT);
            }


            int size = paint.getPicture_info().size();
            for (int i = 0; i < size; i++) {
                Picture item = paint.getPicture_info().get(i);
                //横
                if (item.getPicture_type() == 1) {
                    picturesHList.add(item);
                    //竖
                } else {
                    picturesSList.add(item);
                }

                picturesHSList.add(item);
            }

            numBottomGallery.setText(String.valueOf(picturesHList.size()));

            if (picturesHList.size() == 0) {
                paintRecycler.setVisibility(View.GONE);
                defaultText.setVisibility(VISIBLE);
            } else {
                paintRecycler.setVisibility(VISIBLE);
                defaultText.setVisibility(View.GONE);
                imageGridRecyclerAdapter.notifyDataSetChanged();
            }
            leftBtnGallery.setChecked(true);


            if (HHApplication.loginFlag && paint.getPaint_id() != 0) {

                LocalPaints recentPaints = (LocalPaints) diskLruCacheHelper.getAsSerializable(Constant.LOCAL_RECENT + userId);
                if (recentPaints != null) {

                    ArrayList<Paint> localPaints = recentPaints.getPaints();
                    for (int i = 0; i < localPaints.size(); i++) {
                        if (localPaints.get(i).getPaint_id() == paintId) {
                            localPaints.remove(i);
                            break;
                        }
                    }
                } else {
                    recentPaints = new LocalPaints();
                }

                recentPaints.getPaints().add(0, paint);

                diskLruCacheHelper.put(Constant.LOCAL_RECENT + userId, recentPaints);

                SLogger.d("<<", "--recent--------------->>>" + JSON.toJSONString(recentPaints));

                EventBus.getDefault().post(new AddRecentEvent());
            }

            //设置点击各个地方的  调转到播放
            diskLruCacheHelper.put(Constant.LAST_PAINT, paint);


        } catch (Exception e) {
            e.printStackTrace();
        }
//        } else {
//            for (int i = 0; i < paint.getPicture_num(); i++) {
//                Picture item = paint.getPicture_info().get(i);
//                //横
//                if (item.getPicture_type() == 1) {
//                    picturesHList.add(item);
//                    //竖
//                } else {
//                    picturesSList.add(item);
//                }
//
//                picturesHSList.add(item);
//            }
//
//            //设置点击各个地方的  调转到播放
//            diskLruCacheHelper.put(Constant.LAST_PAINT, paint);
//
//            imageGridRecyclerAdapter.notifyDataSetChanged();
//        }

        last_id = paint.getLast_id();

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
        if (last_id != 0) {
            --last_id;
        }
    }
}
