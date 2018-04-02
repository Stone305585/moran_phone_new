package com.phone.moran.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.phone.moran.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnimActivity extends BaseActivity {

    @BindView(R.id.animFL)
    FrameLayout animFL;
    @BindView(R.id.register)
    TextView register;
    @BindView(R.id.later_btn)
    TextView laterBtn;
    @BindView(R.id.back_btn)
    TextView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anim);
        ButterKnife.bind(this);

        initView();

        setListener();
    }

    @Override
    protected void initView() {
        super.initView();
//        startActivity(new Intent(AnimActivity.this, LoginActivity.class));
//        finish();
        final AnimationDrawable ad = (AnimationDrawable) animFL.getBackground();
        ad.start();
    }

    @Override
    protected void setListener() {
        super.setListener();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        laterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnimActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
