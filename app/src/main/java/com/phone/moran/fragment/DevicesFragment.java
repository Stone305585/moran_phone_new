package com.phone.moran.fragment;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.phone.moran.HHApplication;
import com.phone.moran.MainActivity;
import com.phone.moran.R;
import com.phone.moran.adapter.DeviceListAdapter;
import com.phone.moran.event.BindSuccess;
import com.phone.moran.event.LoginEvent;
import com.phone.moran.event.LogoutEvent;
import com.phone.moran.model.DeviceInfo;
import com.phone.moran.model.User;
import com.phone.moran.presenter.implPresenter.UserBandActivityImpl;
import com.phone.moran.presenter.implView.IUserBandActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DevicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DevicesFragment extends BaseFragment implements View.OnClickListener , IUserBandActivity{

    public static final String PAGE_FLAG = "devices_init";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.add_device)
    LinearLayout addDevice;
    @BindView(R.id.wifi_setting)
    LinearLayout wifiSetting;
    Unbinder unbinder;
    @BindView(R.id.devices_des_tv)
    TextView devicesDesTv;

    private String mParam1;
    private String mParam2;

    private WifiListFragment wifiListFragment;
    private ScanCodeFragment scanCodeFragment;
    private MineFragment mineFragment;
    private FragmentManager fm;

    //--------pop-------------
    private TextView numTv;
    private RecyclerView recyclerView;
    private DeviceListAdapter deviceListAdapter;
    private List<DeviceInfo> deviceInfos = new ArrayList<>();
    private UserBandActivityImpl userBandActivityImpl;

    private DeviceInfo deviceInfo;
    private int pos = -1;

    public DevicesFragment() {
        // Required empty public constructor
    }

    public MineFragment getMineFragment() {
        return mineFragment;
    }

    public void setMineFragment(MineFragment mineFragment) {
        this.mineFragment = mineFragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DevicesFragment.
     */
    public static DevicesFragment newInstance(String param1, String param2) {
        DevicesFragment fragment = new DevicesFragment();
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
        EventBus.getDefault().register(this);
        userBandActivityImpl = new UserBandActivityImpl(getActivity(), this);

//        deviceIds = JSON.parseArray(diskLruCacheHelper.getAsString(Constant.PUSH_DEVICES), String.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        unbinder = ButterKnife.bind(this, view);

        initView(view);
        initPopWin();

        setListener();

        if(HHApplication.loginFlag) {
            userBandActivityImpl.getDeviceList();
        }
        return view;
    }

    @Override
    protected void initView(View view) {

        super.initView(view);
    }

    public void initPopWin() {
        popView = LayoutInflater.from(getActivity()).inflate(R.layout.pop_devices_list, null);
        popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setContentView(popView);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(getResources().getColor(R.color.white));
        popupWindow.setBackgroundDrawable(dw);
        numTv = (TextView) popView.findViewById(R.id.num_bands);
        recyclerView = (RecyclerView) popView.findViewById(R.id.recycler_devices);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        deviceListAdapter = new DeviceListAdapter(getActivity(), deviceInfos);
        recyclerView.setAdapter(deviceListAdapter);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 1f;
                getActivity().getWindow().setAttributes(params);
            }
        });

    }

    private List<String> deviceIds = new ArrayList<>();
    @Override
    protected void setListener() {
        super.setListener();

        wifiSetting.setOnClickListener(this);

        addDevice.setOnClickListener(this);

        devicesDesTv.setOnClickListener(this);

        deviceListAdapter.setmBandListener(new DeviceListAdapter.BandListener() {
            @Override
            public void remove(int pos11, View v, DeviceInfo user) {
                deviceInfo = user;
                pos = pos11;
                userBandActivityImpl.remove(user.getDevice_id());
            }

            @Override
            public void check(int pos, final CheckBox v1, boolean isCheck, final DeviceInfo device) {
                if(isCheck) {
                    showMyDialog(getResources().getString(R.string.push_to_device), 0);

                    setAlterListener(new AlterDialogInterface() {
                        @Override
                        public void positiveGo() {

                            //添加该设备
                            if(!deviceIds.contains(device.getDevice_id())) {
                                deviceIds.add(device.getDevice_id());
                                device.setFlag(1);
                            }

                            ad.dismiss();
                        }

                        @Override
                        public void negativeGo() {
                            v1.setChecked(false);

                            ad.dismiss();
                        }
                    });
                } else {
//                    if(deviceIds.size() == 1) {
//                        showMyDialog("还是保留一个推送设备吧", 1);
//                        setAlterListener(new AlterDialogInterface() {
//                            @Override
//                            public void positiveGo() {
//                                ad.dismiss();
//
//                                v1.setChecked(true);
//                            }
//
//                            @Override
//                            public void negativeGo() {
//
//                            }
//                        });
//                    } else {
                        showMyDialog(getResources().getString(R.string.discon_device), 0);

                        setAlterListener(new AlterDialogInterface() {
                            @Override
                            public void positiveGo() {

                                if(deviceIds.contains(device.getDevice_id())) {
                                    deviceIds.remove(device.getDevice_id());
                                    device.setFlag(2);
                                }

                                ad.dismiss();
                            }

                            @Override
                            public void negativeGo() {
                                v1.setChecked(true);

                                ad.dismiss();
                            }
                        });

//                    }

                }
            }
        });

//        deviceListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position, Object model) {
//                DeviceInfo deviceInfo = (DeviceInfo) model;
//                if(deviceInfo.getFlag() == 1) {
//                    Intent intent = new Intent(getActivity(), UserBandActivity.class);
//                    intent.putExtra(Constant.DEVICE_ID, deviceInfo.getDevice_id());
//                    startActivity(intent);
//                }
//            }
//
//            @Override
//            public void onItemLongClick(View view, int position, Object model) {
//
//            }
//        });


//        addDevice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!HHApplication.loginFlag) {
//                    AppUtils.showToast(getActivity().getApplicationContext(), "请您登陆后添加设备");
//                }
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {

        if (goLogin()) {
            return;
        }
        switch (v.getId()) {
            case R.id.wifi_setting:

//                FragmentTransaction ft = fm.beginTransaction();
                if (wifiListFragment == null) {
                    wifiListFragment = WifiListFragment.newInstance("", "");
//                    ft.add(R.id.mine_content, wifiListFragment);
                }

//                ft.show(wifiListFragment);
//                ft.commit();
                wifiListFragment.setMineFragment(mineFragment);

                mineFragment.showFragment(wifiListFragment);

                ((MainActivity) getActivity()).getMineFragment().showBack();

                break;

            case R.id.add_device:
//                FragmentTransaction ft1 = fm.beginTransaction();
                if (scanCodeFragment == null) {
                    scanCodeFragment = ScanCodeFragment.newInstance("", "");
//                    ft1.add(R.id.mine_content, scanCodeFragment);
                }
//                ((MainActivity)getActivity()).setMineF(scanCodeFragment);

                mineFragment.showFragment(scanCodeFragment);

                ((MainActivity) getActivity()).getMineFragment().showBack();
//                ft1.show(scanCodeFragment);
//                ft1.commit();


                break;

            case R.id.devices_des_tv:
                if(deviceInfos.size() != 0) {
                    popupWindow.showAtLocation(devicesDesTv, Gravity.BOTTOM, 0, 0);
                    // 设置popWindow弹出窗体可点击
                    popupWindow.setFocusable(true);
                    WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                    params.alpha = 0.7f;
                    getActivity().getWindow().setAttributes(params);

                    deviceListAdapter.notifyDataSetChanged();
                }
                break;
        }

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            mineFragment.hideBack();
        }
    }

    /**
     * 暂时用来测试
     *
     * @param event
     */
    public void onEventMainThread(BindSuccess event) {

        userBandActivityImpl.getDeviceList();

    }


    /**
     * 暂时用来测试
     *
     * @param event
     */
    public void onEventMainThread(LoginEvent event) {

        userBandActivityImpl.getDeviceList();

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
//        AppUtils.showToast(getActivity().getApplicationContext(), error);
    }

    @Override
    public void remove() {
//        AppUtils.showToast(getActivity().getApplicationContext(), "解除绑定成功！");
        if(deviceInfo != null && pos != -1) {
            deviceListAdapter.remove(pos);
            deviceListAdapter.notifyDataSetChanged();
            numTv.setText(getResources().getString(R.string.has_bind_num_device_msg) + deviceListAdapter.getItemCount());
        }

        if(deviceListAdapter.getItemCount() == 0) {
            devicesDesTv.setText(getResources().getString(R.string.no_device));
        }
    }

    @Override
    public void approve() {

    }

    @Override
    public void updateMain(List<User> list) {

    }

    @Override
    public void updateMainDevice(List<DeviceInfo> list) {

        if(list.size() != 0) {

            deviceInfos.clear();
            deviceInfos.addAll(list);

            devicesDesTv.setText(getResources().getString(R.string.device_manage));
//            deviceListAdapter.notifyDataSetChanged();
            numTv.setText(getResources().getString(R.string.has_bind_num_device_msg) + list.size());
        }

    }

    public WifiListFragment getWifiListFragment() {
        return wifiListFragment;
    }

    public void setWifiListFragment(WifiListFragment wifiListFragment) {
        this.wifiListFragment = wifiListFragment;
    }
    public void onEventMainThread(LogoutEvent event) {

        devicesDesTv.setText(getResources().getString(R.string.no_device));
        deviceInfos.clear();

    }

}
