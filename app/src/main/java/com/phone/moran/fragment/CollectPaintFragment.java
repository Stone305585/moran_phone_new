package com.phone.moran.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.phone.moran.R;
import com.phone.moran.activity.PaintActivity;
import com.phone.moran.adapter.LocalRecyclerAdapter;
import com.phone.moran.config.Constant;
import com.phone.moran.event.CollectEvent;
import com.phone.moran.event.LogoutEvent;
import com.phone.moran.model.LocalPaintArray;
import com.phone.moran.model.LocalPaints;
import com.phone.moran.model.Paint;
import com.phone.moran.tools.PreferencesUtils;
import com.phone.moran.tools.SLogger;
import com.phone.moran.tools.diskCache.DiskLruCacheHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectPaintFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectPaintFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.recycler_mine_p)
    RecyclerView recyclerMineP;
    Unbinder unbinder;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    LocalRecyclerAdapter localRecyclerAdapter;
    private List<LocalPaintArray> list = new ArrayList<>();
    private boolean needRefresh = false;


    public CollectPaintFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MinePaintFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollectPaintFragment newInstance(String param1, String param2) {
        CollectPaintFragment fragment = new CollectPaintFragment();
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

        localRecyclerAdapter = new LocalRecyclerAdapter(getActivity(), list);

        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mine_paint, container, false);
        unbinder = ButterKnife.bind(this, v);

        initView(v);
        setListener();
        return v;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        initLocalCollect();

        recyclerMineP.setItemAnimator(new DefaultItemAnimator());
        recyclerMineP.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerMineP.setAdapter(localRecyclerAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(needRefresh) {
            try {
                diskLruCacheHelper = new DiskLruCacheHelper(getActivity());

            } catch (IOException e) {
                e.printStackTrace();
            }

            initLocalCollect();
            localRecyclerAdapter.notifyDataSetChanged();
            needRefresh = false;
        }
    }

    /**
     * 填充本地的收藏的list
     */
    private void initLocalCollect() {


        userId = PreferencesUtils.getString(getActivity(), Constant.USER_ID);

        if (diskLruCacheHelper.getAsSerializable(Constant.LOCAL_COLLECT + userId) != null) {

            list.clear();

            ArrayList<Paint> localPaints = ((LocalPaints) diskLruCacheHelper.getAsSerializable(Constant.LOCAL_COLLECT + userId)).getPaints();

            SLogger.d("<<", "-->>COLLECT--->>>" + JSON.toJSONString(localPaints));

            int size = localPaints.size();

            //一共几行
            int rows = (int) Math.ceil((float)size / 3);

            //剩下几个不够一行
            int rest = size % 3;

            if (rows > 1 && rest != 0) {
                for (int j = 0; j < rows - 1; j++) {
                    LocalPaintArray l = new LocalPaintArray();
                    l.setPaint1(localPaints.get(3 * j));
                    l.setPaint2(localPaints.get(3 * j + 1));
                    l.setPaint3(localPaints.get(3 * j + 2));
                    list.add(l);
                }

            } else if (rest == 0 && rows > 0) {
                for (int k = 0; k < rows; k++) {
                    LocalPaintArray l = new LocalPaintArray();
                    l.setPaint1(localPaints.get(3 * k));
                    l.setPaint2(localPaints.get(3 * k + 1));
                    l.setPaint3(localPaints.get(3 * k + 2));
                    list.add(l);
                }
            }

            if (rest > 0) {

                LocalPaintArray localPaintArray = new LocalPaintArray();

                if (rest > 0) {
                    localPaintArray.setPaint1(localPaints.get((rows - 1) * 3));
                }
                if (rest > 1) {
                    localPaintArray.setPaint2(localPaints.get((rows - 1) * 3 + 1));
                }
                if (rest > 2) {
                    localPaintArray.setPaint3(localPaints.get((rows - 1) * 3 + 2));
                }

                list.add(localPaintArray);
            }

        }

        if(list.size() == 0) {
            LocalPaintArray localPaintArray1 = new LocalPaintArray();
            list.add(localPaintArray1);
            LocalPaintArray localPaintArray2 = new LocalPaintArray();
            list.add(localPaintArray2);
            LocalPaintArray localPaintArray3 = new LocalPaintArray();
            list.add(localPaintArray3);
        }else if(list.size() < 4) {
            LocalPaintArray localPaintArray1 = new LocalPaintArray();
            list.add(localPaintArray1);
            LocalPaintArray localPaintArray2 = new LocalPaintArray();
            list.add(localPaintArray2);
        } else if(list.size() < 7) {
            LocalPaintArray localPaintArray2 = new LocalPaintArray();
            list.add(localPaintArray2);
        }

    }

    @Override
    protected void setListener() {
        super.setListener();

        localRecyclerAdapter.setmBandListener(new LocalRecyclerAdapter.BandListener() {
            @Override
            public void onItemClick(int pos, int itemPos, View v, Paint localPaint) {

                if (goLogin()) {
                    return;
                }

                Intent intent = new Intent(getActivity(), PaintActivity.class);
                intent.putExtra(Constant.PAINT_ID, localPaint.getPaint_id());
                startActivity(intent);
            }

            @Override
            public void onLongClick(int pos, int itemPos, View v, Paint localPaint) {
//                if (closeView != null)
//                    closeView.setVisibility(View.GONE);
//                closeView = v;

            }

            @Override
            public void onCloseClick(int pos, int itemPos, View v, Paint localPaint) {
                LocalPaints localPaints = diskLruCacheHelper.getAsSerializable(Constant.LOCAL_COLLECT + userId);

                for (int i = 0; i < localPaints.getPaints().size(); i++) {
                    if (localPaints.getPaints().get(i).getPaint_id() == localPaint.getPaint_id()) {
                        localPaints.getPaints().remove(i);
                        break;
                    }
                }
                diskLruCacheHelper.put(Constant.LOCAL_COLLECT + userId, localPaints);

                dialog.show();
                initLocalCollect();
                localRecyclerAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void initDataSource() {
        super.initDataSource();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void onEventMainThread(CollectEvent event) {

        needRefresh = true;
//        initLocalCollect();
//        localRecyclerAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(LogoutEvent event) {

        list.clear();
        localRecyclerAdapter.notifyDataSetChanged();
    }

}
