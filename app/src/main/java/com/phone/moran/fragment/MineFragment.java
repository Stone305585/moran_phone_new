package com.phone.moran.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.phone.moran.MainActivity;
import com.phone.moran.R;
import com.phone.moran.activity.PlayPictureActivity;
import com.phone.moran.activity.SettingActivity;
import com.phone.moran.activity.UserInfoActivity;
import com.phone.moran.config.Constant;
import com.phone.moran.event.LogoutEvent;
import com.phone.moran.model.Paint;
import com.phone.moran.tools.ImageLoader;
import com.phone.moran.tools.PreferencesUtils;
import com.phone.moran.tools.SLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MineFragment extends BaseFragment implements View.OnClickListener {

    public static final String PAGE_FLAG = "mine_frag";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.setting_btn)
    ImageView settingBtn;
    @BindView(R.id.edit_mine_btn)
    ImageView editMineBtn;
    @BindView(R.id.mine_content)
    FrameLayout mineContent;
    Unbinder unbinder;
    @BindView(R.id.back_btn)
    ImageView backBtn;
    @BindView(R.id.title_mine)
    TextView titleMine;
    @BindView(R.id.mine_bg_fl)
    ImageView mineBg;
    @BindView(R.id.name_tv)
    TextView nameTv;
    @BindView(R.id.show_btn)
    ImageView showBtn;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentManager fm;
    private DevicesFragment devicesFragment;

    private String flag = PAGE_FLAG;
    private MainActivity mainActivity;


    public MineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MineFragment newInstance(String param1, String param2) {
        MineFragment fragment = new MineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        fm = getActivity().getSupportFragmentManager();
        devicesFragment = DevicesFragment.newInstance("", "");
        devicesFragment.setMineFragment(this);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mine, container, false);
        unbinder = ButterKnife.bind(this, v);

        initView(v);
        setListener();
        initDataSource();
        return v;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        hideBack();

        mainActivity.setFlag(MainActivity.DEVICE);
        mainActivity.setMineF(devicesFragment);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.mine_content, devicesFragment);
        ft.show(devicesFragment);
        ft.commit();
    }

    @Override
    protected void setListener() {
        super.setListener();

        editMineBtn.setOnClickListener(this);
        settingBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        showBtn.setOnClickListener(this);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_mine_btn:
                if (!goLogin()) {
                    startActivity(new Intent(getActivity(), UserInfoActivity.class));
                }
                break;

            case R.id.setting_btn:
                if(!goLogin()) {
                    startActivity(new Intent(getActivity(), SettingActivity.class));
                }
                break;

            case R.id.back_btn:

                SLogger.d("<<", "--->>>close");
                String flag = mainActivity.getFlag();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(mainActivity.getMineF());
                if(mainActivity.getMineF() instanceof DevicesFragment) {
                    hideBack();
                }
//                if (mainActivity.getMineF() instanceof WifiListFragment || mainActivity.getMineF() instanceof ScanCodeFragment){
//                    SLogger.d("<<", "++++====>>1111");
                showFragment(devicesFragment);
//                }
//                else if (mainActivity.getMineF() instanceof WifiConFragment) {
//                    SLogger.d("<<", "++++====>>22222");
//                    showFragment(devicesFragment.getWifiListFragment());
//                }
//
//                switch (flag) {
//                    case MainActivity.DEVICE:
//                        break;
//                    case MainActivity.WIFI:
//                        break;
//                    case MainActivity.SCAN:
//
//                        break;
//                    case MainActivity.CONWIFI:
//                        break;
//                }
                ft.commit();
                break;

            case R.id.show_btn:
                Paint paint = diskLruCacheHelper.getAsSerializable(Constant.LAST_PAINT);
                Intent intent = new Intent(getActivity(), PlayPictureActivity.class);
                intent.putExtra(Constant.PAINT, paint);
                startActivity(intent);
                break;

        }
    }

    public void showBack() {
        backBtn.setVisibility(View.VISIBLE);
        titleMine.setVisibility(View.INVISIBLE);
    }

    public void hideBack() {
        backBtn.setVisibility(View.INVISIBLE);
        titleMine.setVisibility(View.VISIBLE);
    }

    public void showFragment(BaseFragment fragment) {
        FragmentTransaction ft = fm.beginTransaction();

        if (!fragment.isAdded())
            ft.add(R.id.mine_content, fragment);
        ft.hide(mainActivity.getMineF());

        ((MainActivity) getActivity()).setMineF(fragment);

        ft.show(fragment);
        ft.commit();

    }

    @Override
    public void onResume() {
        super.onResume();

        String username = PreferencesUtils.getString(getActivity(), Constant.USER_NAME);
        String headUrl = PreferencesUtils.getString(getActivity(), Constant.AVATAR);
        String bgUrl = PreferencesUtils.getString(getActivity(), Constant.MINE_BG);

        if (!TextUtils.isEmpty(headUrl))
            ImageLoader.displayImg(getActivity(), headUrl, editMineBtn);
        else {
            editMineBtn.setImageDrawable(getResources().getDrawable(R.mipmap.mine_default_head));
        }
        if (!TextUtils.isEmpty(bgUrl))
            ImageLoader.displayImg(getActivity(), bgUrl, mineBg);
        else
            mineBg.setImageDrawable(getResources().getDrawable(R.mipmap.mine_head_bg));
        if (!TextUtils.isEmpty(username))
            nameTv.setText(username);
        else
            nameTv.setText("未设置");

    }

    public void onEventMainThread(LogoutEvent event) {

        editMineBtn.setImageDrawable(getResources().getDrawable(R.mipmap.mine_default_head));
        mineBg.setImageDrawable(getResources().getDrawable(R.mipmap.mine_head_bg));
        nameTv.setText("未设置");

    }
}
