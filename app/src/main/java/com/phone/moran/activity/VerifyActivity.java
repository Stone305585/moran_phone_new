package com.phone.moran.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phone.moran.HHApplication;
import com.phone.moran.MainActivity;
import com.phone.moran.R;
import com.phone.moran.config.Constant;
import com.phone.moran.model.LocalMoods;
import com.phone.moran.model.LocalPaints;
import com.phone.moran.model.Mood;
import com.phone.moran.model.Paint;
import com.phone.moran.model.RegisterBack;
import com.phone.moran.presenter.implPresenter.RegisterActivityImpl;
import com.phone.moran.presenter.implView.IRegisterActivity;
import com.phone.moran.tools.AppUtils;
import com.phone.moran.tools.PreferencesUtils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.phone.moran.fragment.MobileRegisterFragment.PASSWORD;
import static com.phone.moran.fragment.MobileRegisterFragment.PHONE;

/**
 * 验证码 页面
 */
public class VerifyActivity extends BaseActivity implements IRegisterActivity {

    @BindView(R.id.code_et)
    EditText codeEt;
    @BindView(R.id.next_btn)
    Button nextBtn;


    RegisterActivityImpl registerActivityImpl;
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
    @BindView(R.id.time_resend)
    TextView timeResend;
    private String phone;
    private String password;
    private int defaultTime = 60;//s
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        ButterKnife.bind(this);

        timer = new Timer();
        initView();
        setListener();

        registerActivityImpl = new RegisterActivityImpl(this, token, this);
        phone = getIntent().getStringExtra(PHONE);
        password = getIntent().getStringExtra(PASSWORD);
    }

    @Override
    protected void initView() {
        super.initView();

        startTimer();
    }

    private void startTimer() {
        defaultTime = 60;
        timeResend.setEnabled(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (defaultTime == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeResend.setEnabled(true);
                            timeResend.setText("重新发送");
                            timer.cancel();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeResend.setText(String.valueOf(--defaultTime) + "s");
                        }
                    });
                }
            }
        }, 1000, 1000);
    }

    @Override
    protected void setListener() {
        super.setListener();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeEt.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    AppUtils.showToast(getApplicationContext(), "请输入验证码");
                } else {
                    registerActivityImpl.register(phone, password, code);
                }
            }
        });

        timeResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
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
        AppUtils.showToast(getApplicationContext(), error);
    }

    @Override
    public void registerSuccess(RegisterBack registerBack) {
        PreferencesUtils.putString(getApplicationContext(), Constant.USER_ID, String.valueOf(registerBack.getUin()));
        PreferencesUtils.putString(getApplicationContext(), Constant.ACCESS_TOKEN, registerBack.getToken());
        HHApplication.checkLogin();

        userId = String.valueOf(registerBack.getUin());

        LocalPaints l = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MINE + userId);
        if(l == null)
            l = new LocalPaints();
        //获取本地的 local collect  为空则放入默认的画单  我的收藏
        if(l.getPaints() == null || l.getPaints().size() == 0) {
            ArrayList<Paint> paints = new ArrayList<>();
            Paint p = new Paint();
            p.setPaint_id(-1);
            p.setPaint_title("我的收藏");
            paints.add(p);
            l.setPaints(paints);
            diskLruCacheHelper.put(Constant.LOCAL_MINE + userId, l);
        }

        //存放本地心情初始化
        LocalMoods lm = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_MOOD + userId);

        if(lm == null) {
            lm = new LocalMoods();
            ArrayList<Mood> moodList = new ArrayList<>();

            Mood moodNu = new Mood();
            moodNu.setMood_id(Constant.MOOD_NU);
            moodNu.setMood_name("怒");
            moodNu.setRes_id(R.mipmap.mood_nu);
            moodList.add(moodNu);

            Mood moodSi = new Mood();
            moodSi.setMood_name("思");
            moodNu.setMood_id(Constant.MOOD_SI);
            moodSi.setRes_id(R.mipmap.mood_si);
            moodList.add(moodSi);

            Mood moodKong = new Mood();
            moodKong.setMood_id(Constant.MOOD_KONG);
            moodKong.setMood_name("恐");
            moodKong.setRes_id(R.mipmap.mood_kong);
            moodList.add(moodKong);

            Mood moodJing = new Mood();
            moodJing.setMood_id(Constant.MOOD_JING);
            moodJing.setMood_name("惊");
            moodJing.setRes_id(R.mipmap.mood_jing);
            moodList.add(moodJing);

            Mood moodYou = new Mood();
            moodYou.setMood_id(Constant.MOOD_YOU);
            moodYou.setMood_name("忧");
            moodYou.setRes_id(R.mipmap.mood_you);
            moodList.add(moodYou);

            Mood moodXi = new Mood();
            moodXi.setMood_id(Constant.MOOD_XI);
            moodXi.setMood_name("喜");
            moodXi.setRes_id(R.mipmap.mood_xi);
            moodList.add(moodXi);

            Mood moodBei = new Mood();
            moodBei.setMood_id(Constant.MOOD_BEI);
            moodBei.setMood_name("悲");
            moodBei.setRes_id(R.mipmap.mood_bei);
            moodList.add(moodBei);

            Mood moodWu = new Mood();
            moodWu.setMood_id(Constant.MOOD_WU);
            moodWu.setMood_name("空");
            moodWu.setRes_id(R.mipmap.mood_wu);
            moodList.add(moodWu);

            lm.setMoods(moodList);

            diskLruCacheHelper.put(Constant.LOCAL_MOOD + userId, lm);

        }

        startActivity(new Intent(this, MainActivity.class));

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

    }
}