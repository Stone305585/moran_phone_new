package com.phone.moran.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.phone.moran.R;
import com.phone.moran.adapter.BaseRecyclerAdapter;
import com.phone.moran.adapter.MainRecyclerAdaper;
import com.phone.moran.adapter.MineCollectAdapter;
import com.phone.moran.adapter.MoodRoundGridAdapter;
import com.phone.moran.config.Constant;
import com.phone.moran.event.AddMineEvent;
import com.phone.moran.model.LocalMoods;
import com.phone.moran.model.LocalPaints;
import com.phone.moran.model.Mood;
import com.phone.moran.model.Paint;
import com.phone.moran.model.Picture;
import com.phone.moran.presenter.implPresenter.PlayPictureActivityImpl;
import com.phone.moran.presenter.implView.IPlayPictureActivity;
import com.phone.moran.tools.AppUtils;
import com.phone.moran.tools.SLogger;
import com.phone.moran.view.gallery.CardScaleHelper;
import com.phone.moran.view.gallery.SpeedRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class PlayPictureActivity extends BaseActivity implements View.OnClickListener, IPlayPictureActivity {

    public static final int SINGLE = 3;
    public static final int RAND = 1;
    public static final int ALL = 2;

    public static final int NORMAL = 20;
    public static final int NIGHT = 21;
    public static final int SLEEP = 22;

    public static final int PAINT = 10;
    public static final int PICTURE = 11;

    public static final int NEW_PAINT_RES = 300;

    @BindView(R.id.recycler_picture)
    SpeedRecyclerView recyclerPicture;
    @BindView(R.id.play_mode_btn)
    ImageView playModeBtn;
    @BindView(R.id.play_tip_btn)
    ImageView playTipBtn;
    @BindView(R.id.play_upload_btn)
    ImageView playUploadBtn;
    @BindView(R.id.play_lining_btn)
    ImageView playLiningBtn;
    @BindView(R.id.play_more_btn)
    ImageView playMoreBtn;
    View popView;
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
    @BindView(R.id.collect_pop_FL)
    FrameLayout collectPopFL;
    @BindView(R.id.recycler_paints)
    RecyclerView recyclerPaints;

    private LinearLayout detailLL;
    private LinearLayout collectLL;
    private LinearLayout moodLL;
    private ImageView collectPopIv;
    private ImageView minute2;
    private ImageView minute5;
    private ImageView minute15;
    private ImageView minute30;
    private ImageView normalBtn;
    private ImageView nightBtn;
    private ImageView sleepBtn;
    private ImageView normalNode;
    private ImageView nightNode;
    private ImageView sleepNode;

    MainRecyclerAdaper imagePagerAdapter;
    List<Picture> images = new ArrayList<>();
    private CardScaleHelper mCardScaleHelper = null;
    Paint paint;
    Picture picture;
    int playMode = ALL;//默认单张播放  rand 1     order 2    single   3//  1-2-3

    private int mCardWidth = 340;//单个画

    private Timer testTimer;
    private int curI = 0; //当前第几张
    private int recycleTime = 120000; //2分钟、5分钟  300000、15分钟  900000、30分钟  1800000
    private TimerTask timerTask;
    private int pageFlag = 0;//画单0、画作1

    private ObjectAnimator showMineC;
    private ObjectAnimator goneMineC;

    LocalMoods localMoods;
    private PlayPictureActivityImpl playPicImpl;

    private int last_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_picture);
        ButterKnife.bind(this);

        pageFlag = getIntent().getIntExtra(Constant.PLAY_FLAG, PAINT);
        //TODO 这里应该只有PAINT这一种类型
//        if (pageFlag == PAINT) {
        paint = (Paint) getIntent().getSerializableExtra(Constant.PAINT);
        images = paint.getPicture_info();
//        } else {
//            picture = (Picture) getIntent().getSerializableExtra(Constant.PICTURE);
//            images.add(picture);
//        }

        SLogger.d("<<", "-->>>" + images.toString());

        localMoods = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MOOD + userId);
        playPicImpl = new PlayPictureActivityImpl(this, token, this);
        testTimer = new Timer();

        if(images.size() == 0) {
            playPicImpl.getPaintDetail(paint.getPaint_id(), last_id);
        } else {
            last_id = images.get(images.size() - 1).getPicture_id();
        }

        initView();
        initPopWin();
        initMoodPop();
        setListener();
        initDataSource();
    }

    public void initPopWin() {
        popView = LayoutInflater.from(this).inflate(R.layout.pop_tips_more, null);
        popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(popView);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
        popupWindow.setOutsideTouchable(true);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

        detailLL = popView.findViewById(R.id.to_detail_LL);
        collectLL = popView.findViewById(R.id.to_collect_LL);
        moodLL = popView.findViewById(R.id.to_mood_LL);
        collectPopIv = (ImageView) popView.findViewById(R.id.collect_pop);
        minute2 = popView.findViewById(R.id.minute_2);
        minute5 = popView.findViewById(R.id.minute_5);
        minute15 = popView.findViewById(R.id.minute_15);
        minute30 = popView.findViewById(R.id.minute_30);
        normalBtn = popView.findViewById(R.id.normal_iv);
        nightBtn = popView.findViewById(R.id.night_iv);
        sleepBtn = popView.findViewById(R.id.sleep_iv);
        normalNode = popView.findViewById(R.id.normal_node);
        nightNode = popView.findViewById(R.id.night_node);
        sleepNode = popView.findViewById(R.id.sleep_node);

        detailLL.setOnClickListener(this);
        collectLL.setOnClickListener(this);
        moodLL.setOnClickListener(this);
        minute2.setOnClickListener(this);
        minute5.setOnClickListener(this);
        minute15.setOnClickListener(this);
        minute30.setOnClickListener(this);
        normalNode.setOnClickListener(this);
        nightNode.setOnClickListener(this);
        sleepNode.setOnClickListener(this);
        normalBtn.setOnClickListener(this);
        nightBtn.setOnClickListener(this);
        sleepBtn.setOnClickListener(this);
        backTitle.setOnClickListener(this);


        initSelectCircle();

        //初始化为标准模式
        normalNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node_selected));
        lightMode = NORMAL;
    }


    @Override
    protected void initView() {
        super.initView();

        mineCollectAdapter = new MineCollectAdapter(this, localPaints);
        recyclerPaints.setItemAnimator(new DefaultItemAnimator());
        recyclerPaints.setLayoutManager(new LinearLayoutManager(this));
        recyclerPaints.setAdapter(mineCollectAdapter);

        showMineC = ObjectAnimator.ofFloat(collectPopFL, "alpha", 0f, 1f);
        showMineC.setDuration(300);
        showMineC.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                collectPopFL.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        goneMineC = ObjectAnimator.ofFloat(collectPopFL, "alpha", 1f, 0f);
        goneMineC.setDuration(300);
        goneMineC.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                collectPopFL.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        title.setText(paint.getPaint_title());

//        int leftMargin = (ScreenUtils.getScreenWidth(this) - (int) (DensityUtils.dip2px(mCardWidth))) / 2;
//        setViewMargin(recyclerPicture, leftMargin, 0, 0, 0);
            imagePagerAdapter = new MainRecyclerAdaper(this, images);
            recyclerPicture.setItemAnimator(new DefaultItemAnimator());
            recyclerPicture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerPicture.setAdapter(imagePagerAdapter);

        if (images.size() != 0 && mCardScaleHelper == null){

            mCardScaleHelper = new CardScaleHelper();
            mCardScaleHelper.setCurrentItemPos(0);
            mCardScaleHelper.attachToRecyclerView(recyclerPicture, imagePagerAdapter, images);
        }

            /*timerTask = new TimerTask() {
                @Override
                public void run() {
                    curI = ((LinearLayoutManager) recyclerPicture.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCardScaleHelper.setCurrentItemPos(++curI);
                        }
                    });
                }
            };*/



//        testTimer.schedule(timerTask, 3000, recycleTime);
    }

    @Override
    protected void setListener() {
        super.setListener();
        playLiningBtn.setOnClickListener(this);
        playModeBtn.setOnClickListener(this);
        playMoreBtn.setOnClickListener(this);
        playTipBtn.setOnClickListener(this);
        playUploadBtn.setOnClickListener(this);
        collectPopFL.setOnClickListener(this);
        imagePagerAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                Intent intent = new Intent(PlayPictureActivity.this, PictureActivity.class);
                intent.putExtra(Constant.PICTURE_ID, ((Picture) model).getPicture_id());
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });

        recyclerPaints.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager manager = (LinearLayoutManager) recyclerPaints.getLayoutManager();

                int index = manager.findLastVisibleItemPosition();

                if (index >= manager.getItemCount() - 3 && last_id != 0) {
                    playPicImpl.getPaintDetail(paint.getPaint_id(), last_id);
                    last_id = 0;
                }
            }
        });

        mineCollectAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {

                dialog.show();

                Paint paint1 = (Paint) model;

                if (paint1.isMyDefault()) {
                    Intent intent = new Intent(PlayPictureActivity.this, NewPaintActivity.class);
                    startActivityForResult(intent, NEW_PAINT_RES);
                } else {
                    LocalPaints l = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
                    List<Picture> pics = l.getPaintByTitle(paint1.getPaint_title()).getPicture_info();
                    boolean has = false;
                    for (Picture p : pics) {
                        if (p.getPicture_id() == paint.getPicture_info().get(getCurPos()).getPicture_id()) {
                            has = true;
                            break;
                        }
                    }
                    if (has) {
                        dialog.dismiss();
                        AppUtils.showMoranToast(getApplicationContext(), getResources().getString(R.string.collect_success));
                    } else {
                        l.getPaintByTitle(paint1.getPaint_title()).getPicture_info().add(0, paint.getPicture_info().get(getCurPos()));
                        diskLruCacheHelper.put(Constant.LOCAL_MINE + userId, l);
                        paint.getPicture_info().get(getCurPos()).setFlag(Picture.COLLECT);
                        initCollectPop();
                        dialog.dismiss();
                        AppUtils.showMoranToast(getApplicationContext(), getResources().getString(R.string.collect_success));

                        EventBus.getDefault().post(new AddMineEvent());

                    }
                }

            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == NEW_PAINT_RES) {
                String title = data.getStringExtra(InputInfoActivity.INFOS);
                Paint newPaint = new Paint();
                newPaint.setPaint_title(title);
                LocalPaints l = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
                l.getPaints().add(0, newPaint);
                diskLruCacheHelper.put(Constant.LOCAL_MINE + userId, l);

                localPaints.add(1, newPaint);
                mineCollectAdapter.notifyDataSetChanged();

                EventBus.getDefault().post(new AddMineEvent());
            }
        }
    }

    @Override
    protected void initDataSource() {
        super.initDataSource();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_title:
                finish();
                break;
            case R.id.play_mode_btn:
                switch (playMode) {
                    case SINGLE:
                        playMode = ALL;
                        break;
                    case RAND:
                        playMode = SINGLE;
                        break;
                    case ALL:
                        playMode = RAND;
                        break;
                }
                playPicImpl.addPlayMode(playMode);

                break;
            case R.id.play_tip_btn:
                Intent intentTip = new Intent(this, TipActivity.class);
                intentTip.putExtra(Constant.IMAGE, images.get(getCurPos()).getPicture_url());
                startActivity(intentTip);
                break;
            case R.id.play_lining_btn:
                Intent intent = new Intent(this, LiningActivity.class);
                intent.putExtra(Constant.IMAGE, images.get(getCurPos()).getPicture_url());
                startActivity(intent);
                break;
            case R.id.play_upload_btn:

                playPicImpl.playPicture(images.get(getCurPos()).getPicture_id());
                break;
            case R.id.play_more_btn:
                SLogger.d("<<", "-collect->>>>>1");
                if (paint.getPicture_info().get(getCurPos()).getFlag() == Picture.COLLECT) {
                    SLogger.d("<<", "-collect->>>>>12");
                    collectPopIv.setImageDrawable(getResources().getDrawable(R.mipmap.collected));
                } else {
                    SLogger.d("<<", "-collect->>>>>13");
                    collectPopIv.setImageDrawable(getResources().getDrawable(R.mipmap.collect));
                }
                popupWindow.showAtLocation(backTitle, Gravity.BOTTOM, 0, 0);
                // 设置popWindow弹出窗体可点击
                popupWindow.setFocusable(true);
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 0.7f;
                getWindow().setAttributes(params);
                break;

            case R.id.to_detail_LL:
                Intent intentDetail = new Intent(PlayPictureActivity.this, PictureActivity.class);
                intentDetail.putExtra(Constant.PICTURE_ID, (images.get(getCurPos())).getPicture_id());
                startActivity(intentDetail);
                break;

            case R.id.to_collect_LL:
                if (paint.getPicture_info().get(getCurPos()).getFlag() == Picture.COLLECT) {

                    dialog.show();

                    Picture pic = paint.getPicture_info().get(getCurPos());

                    pic.setFlag(Picture.UNCOLLECT);

                    //找到并去掉这个picture
                    LocalPaints l = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
                    for (int i = 0; i < l.getPaints().size(); i++) {
                        Paint paint = l.getPaints().get(i);
                        for (int j = 0; j < paint.getPicture_info().size(); j++) {
                            if (pic.getPicture_id() == paint.getPicture_info().get(j).getPicture_id()) {
                                paint.getPicture_info().remove(j);
                                i = paint.getPicture_info().size();
                                break;
                            }
                        }
                    }
                    diskLruCacheHelper.put(Constant.LOCAL_MINE + userId, l);

                    dialog.dismiss();

                    AppUtils.showMoranToast(getApplicationContext(), getResources().getString(R.string.uncollect_success));

                    EventBus.getDefault().post(new AddMineEvent());


                } else {
                    popupWindow.dismiss();
                    showMineC.start();
                    initCollectPop();
                }

                break;

            case R.id.to_mood_LL:
                moodPopWin.showAtLocation(backTitle, Gravity.BOTTOM, 0, 0);
                // 设置popWindow弹出窗体可点击
                moodPopWin.setFocusable(true);
                WindowManager.LayoutParams paramsMood = getWindow().getAttributes();
                paramsMood.alpha = 0.7f;
                getWindow().setAttributes(paramsMood);
                break;

            case R.id.minute_2:
                recycleTime = 120000;
//                testTimer.cancel();
//                testTimer.schedule(timerTask, 0, recycleTime);
                break;
            case R.id.minute_5:
                recycleTime = 300000;
//                testTimer.cancel();
//                testTimer.schedule(timerTask, 0, recycleTime);
                break;
            case R.id.minute_15:
                recycleTime = 900000;
//                testTimer.cancel();
//                testTimer.schedule(timerTask, 0, recycleTime);
                break;
            case R.id.minute_30:
                recycleTime = 1800000;
//                testTimer.cancel();
//                testTimer.schedule(timerTask, 0, recycleTime);
                break;
            //TODO 推送到硬件
            case R.id.normal_iv:
            case R.id.normal_node:
                lightMode = NORMAL;
                playPicImpl.addPlayLight(lightMode);
                break;
            case R.id.night_iv:
            case R.id.night_node:

                lightMode = NIGHT;
                playPicImpl.addPlayLight(lightMode);
                break;
            case R.id.sleep_iv:
            case R.id.sleep_node:

                lightMode = SLEEP;
                playPicImpl.addPlayLight(SLEEP);
                break;

            case R.id.collect_pop_FL:
                goneCollectPop();
                break;
        }
    }

    private void initSelectCircle() {
        normalNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node));
        sleepNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node));
        nightNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (testTimer != null) {
            testTimer.cancel();
            testTimer = null;
        }
    }

    private void setNormal() {

    }

    private void setNight() {

    }

    private void setSleep() {

    }

    private int getCurPos() {
        return ((LinearLayoutManager) recyclerPicture.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }


    MineCollectAdapter mineCollectAdapter;
    List<Paint> localPaints = new ArrayList<>();

    /**
     * 初始化本地收藏的list pop
     */
    private void initCollectPop() {

        LocalPaints l = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
        ArrayList<Paint> paints = l.getPaints();

        Paint paint = new Paint();
        paint.setMyDefault(true);
        paints.add(0, paint);

        localPaints.clear();
        localPaints.addAll(paints);

        mineCollectAdapter.notifyDataSetChanged();

    }

    private void goneCollectPop() {
        goneMineC.start();
    }


    MoodRoundGridAdapter moodRoundGridAdapter;
    List<Mood> moodList = new ArrayList<>();
    PopupWindow moodPopWin;
    View moodPopView;
    RecyclerView moodRecycler;

    /**
     * 初始化心情的popWindow
     */
    private void initMoodPop() {
        moodPopView = LayoutInflater.from(this).inflate(R.layout.pop_add_mood, null);
        moodPopWin = new PopupWindow(moodPopView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        moodPopWin.setContentView(moodPopView);
        moodPopWin.setAnimationStyle(R.style.mypopwindow_anim_style);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(getResources().getColor(R.color.text_red));
        moodPopWin.setBackgroundDrawable(dw);

        moodPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

        moodRecycler = (RecyclerView) moodPopView.findViewById(R.id.recycler_mood);

        moodList.clear();
        moodList.addAll(localMoods.getMoods());

        moodRoundGridAdapter = new MoodRoundGridAdapter(this, moodList, 1);
        moodRecycler.setItemAnimator(new DefaultItemAnimator());
        moodRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        moodRecycler.setAdapter(moodRoundGridAdapter);

        moodRoundGridAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Object model) {
                dialog.show();

                Mood mood = (Mood) model;

                Picture picture = paint.getPicture_info().get(getCurPos());

                Mood item = localMoods.getMoodById(mood.getMood_id());

                boolean has = false;

                for (int k = 0; k < item.getPictures().size(); k++) {
                    if (item.getPictures().get(k).getPicture_id() == picture.getPicture_id()) {
                        AppUtils.showMoranToast(getApplicationContext(), getResources().getString(R.string.add_mood_success));
                        has = true;
                        break;
                    }
                }
                if (!has) {
                    item.getPictures().add(0, picture);
                    AppUtils.showMoranToast(getApplicationContext(), getResources().getString(R.string.add_mood_success));
                    moodPopWin.dismiss();
                }
                diskLruCacheHelper.put(Constant.LOCAL_MOOD + userId, localMoods);
                dialog.dismiss();
            }

            @Override
            public void onItemLongClick(View view, int position, Object model) {

            }
        });


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
        AppUtils.showMoranToast(getApplicationContext(), error);
    }

    @Override
    public void uploadSuccess() {

        AppUtils.showToast(getApplicationContext(), getResources().getString(R.string.push_success));
    }

    @Override
    public void addPlayModeSuccess() {
        String msg = "";
        switch (playMode) {
            case PlayPictureActivity.RAND:
                playMode = RAND;
                playModeBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_mode_rand));
                msg = getResources().getString(R.string.shuffle_play);
                break;
            case PlayPictureActivity.SINGLE:
                playMode = SINGLE;
                playModeBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_mode_single));
                msg = getResources().getString(R.string.single_play);
                break;
            case PlayPictureActivity.ALL:
                playMode = ALL;
                playModeBtn.setImageDrawable(getResources().getDrawable(R.mipmap.play_mode_all));
                msg = getResources().getString(R.string.order_play);
                break;
        }
        AppUtils.showMoranToast(getApplicationContext(), msg);

    }

    int lightMode = NORMAL;

    @Override
    public void addLightModeSuccess() {

        String msg = "";
        initSelectCircle();

        switch (lightMode) {
            case NORMAL:
                normalNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node_selected));

                msg = getResources().getString(R.string.normal);
                break;
            case NIGHT:
                nightNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node_selected));

                lightMode = NIGHT;
                msg = getResources().getString(R.string.night);
                break;
            case SLEEP:
                sleepNode.setImageDrawable(getResources().getDrawable(R.mipmap.bar_node_selected));
                lightMode = SLEEP;

                lightMode = SLEEP;
                msg = getResources().getString(R.string.sleep);
                break;
        }
        AppUtils.showMoranToast(getApplicationContext(), msg);

    }

    @Override
    public void getMainData(Paint p) {
        paint = p;

        last_id = paint.getLast_id();

        images.addAll(p.getPicture_info());

        paint.setPicture_info(images);

        imagePagerAdapter.notifyDataSetChanged();

        if (images.size() != 0 && mCardScaleHelper == null){

            mCardScaleHelper = new CardScaleHelper();
            mCardScaleHelper.setCurrentItemPos(0);
            mCardScaleHelper.attachToRecyclerView(recyclerPicture, imagePagerAdapter, images);
        }
    }
}
