package com.phone.moran;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.phone.moran.activity.BaseActivity;
import com.phone.moran.event.NewPush;
import com.phone.moran.fragment.CategoryFragment;
import com.phone.moran.fragment.DevicesFragment;
import com.phone.moran.fragment.DiscoverFragment;
import com.phone.moran.fragment.EaselFragment;
import com.phone.moran.fragment.MineFragment;
import com.phone.moran.fragment.ScanCodeFragment;
import com.phone.moran.fragment.WifiConFragment;
import com.phone.moran.fragment.WifiListFragment;
import com.phone.moran.tools.AppUtils;
import com.phone.moran.tools.MyActivityManager;
import com.phone.moran.tools.SLogger;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int PERMISSION_CODE_LOCATION = 111;

    public static final String DEVICE = "deviceFragment";
    public static final String WIFI = "wifiFragment";
    public static final String SCAN = "scanFragment";
    public static final String CONWIFI = "conwifiFragment";

    public static final int DISCOVERINDEX = 0;
    private static final int CATEGORYINDEX = 1;
    private static final int EASELINDEX = 2;
    private static final int MINEINDEX = 3;
    @BindView(R.id.fragment_content)
    FrameLayout fragmentContent;
    @BindView(R.id.discover_btn)
    RadioButton discoverBtn;
    @BindView(R.id.category_btn)
    RadioButton categoryBtn;
    @BindView(R.id.easel_btn)
    RadioButton easelBtn;
    @BindView(R.id.mine_btn)
    RadioButton mineBtn;
    @BindView(R.id.tab)
    LinearLayout tab;
    @BindView(R.id.root_coordinator_layout)
    CoordinatorLayout rootCoordinatorLayout;

    private long exitTime;

    private DiscoverFragment discoverFragment;
    private CategoryFragment categoryFragment;
    private EaselFragment easelFragment;
    private MineFragment mineFragment;
    private FragmentManager fm;
    //当前显示fragment 的id
    private int currentId;

    private String flag;//用于minefragment的切换。
    private Fragment mineF;

    public Fragment getMineF() {
        return mineF;
    }

    public void setMineF(Fragment mineF) {
        this.mineF = mineF;

        if(mineF instanceof WifiListFragment) {
            SLogger.d("<<", "-->>111");
        } else if(mineF instanceof WifiConFragment) {
            SLogger.d("<<", "-->>222");
        } else if(mineF instanceof ScanCodeFragment) {
            SLogger.d("<<", "-->>333");
        } else if(mineF instanceof DevicesFragment) {
            SLogger.d("<<", "-->>444");
        }
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        /**
         * 用来判断是否已经登陆
         */
        if (!HHApplication.loginFlag) {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//            return;
        }

        ButterKnife.bind(this);
        //大于6.0动态检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        WifiManager wifiManager  = wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);;
        if (!wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(true);
        }

        initView();

        setListener();

        initDataSource();

    }


    @Override
    protected void initView() {
        super.initView();

        fm = getSupportFragmentManager();
        discoverFragment = DiscoverFragment.newInstance("", "");
        categoryFragment = CategoryFragment.newInstance("", "");
        easelFragment = EaselFragment.newInstance("", "");
        mineFragment = MineFragment.newInstance("", "");

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_content, discoverFragment);
        ft.add(R.id.fragment_content, categoryFragment);
        ft.add(R.id.fragment_content, easelFragment);
        ft.add(R.id.fragment_content, mineFragment);
        ft.commit();

        resetTab(DISCOVERINDEX);
    }

    @Override
    protected void setListener() {
        super.setListener();

        discoverBtn.setOnClickListener(this);
        categoryBtn.setOnClickListener(this);
        easelBtn.setOnClickListener(this);
        mineBtn.setOnClickListener(this);

    }

    /**
     * 设置当前tab的样式以及切换
     *
     * @param index
     */
    public void resetTab(int index) {

        switch (index) {

            case DISCOVERINDEX:
                check(R.id.discover_btn);
                break;

            case CATEGORYINDEX:
                check(R.id.category_btn);
                break;

            case EASELINDEX:
                check(R.id.easel_btn);
                break;

            case MINEINDEX:
                check(R.id.mine_btn);
                break;

            default:
                break;

        }
    }

    /**
     * 点击RadioButton,选择fragment
     *
     * @param resource
     */
    private void check(int resource) {

        currentId = R.id.mine_btn;

        FragmentTransaction ft = fm.beginTransaction();
        discoverBtn.setChecked(false);
        categoryBtn.setChecked(false);
        easelBtn.setChecked(false);
        mineBtn.setChecked(false);
        ft.hide(discoverFragment);
        ft.hide(categoryFragment);
        ft.hide(easelFragment);
        ft.hide(mineFragment);

        switch (resource) {
            case R.id.discover_btn:
                discoverBtn.setChecked(true);
                ft.show(discoverFragment);
                break;
            case R.id.category_btn:
                categoryBtn.setChecked(true);
                ft.show(categoryFragment);
                break;
            case R.id.easel_btn:
                easelBtn.setChecked(true);
                ft.show(easelFragment);
                break;
            case R.id.mine_btn:
                SLogger.d("<<", "-->>>>5555555555555555");

                mineBtn.setChecked(true);
                ft.show(mineFragment);
                break;
        }
        ft.commit();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                AppUtils.showToast(getApplicationContext(), getResources().getString(R.string.exit_fenghuo));
                exitTime = System.currentTimeMillis();
            } else {
                MyActivityManager.getInstance().finishAllActivity();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CODE_LOCATION) {
            if ((permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

            } else {
                //用户不同意，向用户展示该权限作用
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage(getResources().getString(R.string.need_permission))
                            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                }
                            }).create();
                    dialog.show();
                    return;
                }
            }
        }
    }

    String[] mPermissionList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_LOGS,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SET_DEBUG_APP,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_APN_SETTINGS,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.GET_ACCOUNTS};

    /**
     * ANDROID 6.0权限申请。。
     */
    private void checkPermission() {

        int permissionLocation1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionLocation2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionLocation4 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionLocation6 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int permissionLocation5 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionLocation3 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionLocation1 != PackageManager.PERMISSION_GRANTED ||
                permissionLocation2 != PackageManager.PERMISSION_GRANTED ||
                permissionLocation3 != PackageManager.PERMISSION_GRANTED ||
                permissionLocation4 != PackageManager.PERMISSION_GRANTED ||
                permissionLocation6 != PackageManager.PERMISSION_GRANTED ||
                permissionLocation5 != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    mPermissionList,
                    PERMISSION_CODE_LOCATION);

        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.discover_btn:
                resetTab(DISCOVERINDEX);
                break;
            case R.id.category_btn:
                resetTab(CATEGORYINDEX);
                break;
            case R.id.easel_btn:
                resetTab(EASELINDEX);
                break;
            case R.id.mine_btn:
                resetTab(MINEINDEX);
                break;

            default:
                break;
        }

    }

    @Override
    protected void initDataSource() {
        super.initDataSource();

        if (HHApplication.loginFlag) {
//            MainActivityImpl impl = new MainActivityImpl(this, token, this);
//            impl.getUserinfo();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
//
//
//    @Override
//    public void updateUser(UserInfo user) {
//        usernameTv.setText(user.getUsername());
//        ImageLoader.displayImg(this, user.getHead_image(), headImageIv);
//
//        SLogger.d("<<", "--..>>" + user.getHead_image());
//    }
//
//    @Override
//    public void showProgressDialog() {
//        dialog.show();
//    }
//
//    @Override
//    public void hidProgressDialog() {
//        dialog.dismiss();
//    }
//
//    @Override
//    public void showError(String error) {
//        AppUtils.showToast(getApplicationContext(), error);
//    }

    /**
     * 暂时用来测试
     *
     * @param event
     */
    public void onEventMainThread(NewPush event) {

    }

    public MineFragment getMineFragment() {
        return mineFragment;
    }

    public void setMineFragment(MineFragment mineFragment) {
        this.mineFragment = mineFragment;
    }


}
