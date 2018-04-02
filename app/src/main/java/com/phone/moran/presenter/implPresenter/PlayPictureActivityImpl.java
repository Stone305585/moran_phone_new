package com.phone.moran.presenter.implPresenter;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.phone.moran.activity.PlayPictureActivity;
import com.phone.moran.activity.XinqingActivity;
import com.phone.moran.config.Constant;
import com.phone.moran.model.Back;
import com.phone.moran.model.PaintBack;
import com.phone.moran.presenter.IPlayPictureActivityPresenter;
import com.phone.moran.tools.SLogger;
import com.phone.moran.tools.net.RetrofitUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ASUS on 2017/11/8.
 */
public class PlayPictureActivityImpl extends BasePresenterImpl implements IPlayPictureActivityPresenter {
    private PlayPictureActivity paintActivity;
    private XinqingActivity xinqingActivity;
    private String token;

    public PlayPictureActivityImpl(Context context, String token, PlayPictureActivity paintActivity) {
        super(context);

        this.paintActivity = paintActivity;
        this.token = token;
    }

    @Override
    public void addPlayMode(final int playMode) {
        Map<String, Integer> map = new HashMap<>();
        map.put("play_type", playMode);
        final Subscription subscription = RetrofitUtils.api()
                .modifyPlayType(getBody(map))
                .map(new Func1<Back, Back>() {
                    @Override
                    public Back call(Back back) {
                        return back;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Back>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        paintActivity.hidProgressDialog();
                        paintActivity.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(Back allJourneysBack) {

                        paintActivity.hidProgressDialog();

                        if (allJourneysBack.getRet() == Constant.SUCCESSRESPONSE) {

                            try {
                                paintActivity.addPlayModeSuccess();
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }

                        } else {
                            paintActivity.showError(allJourneysBack.getErr());
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void addPlayTime(int playTime) {
        Map<String, Integer> map = new HashMap<>();
        map.put("play_type", playTime);
        final Subscription subscription = RetrofitUtils.api()
                .modifyPlayType(getBody(map))
                .map(new Func1<Back, Back>() {
                    @Override
                    public Back call(Back back) {
                        return back;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Back>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        paintActivity.hidProgressDialog();
                        paintActivity.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(Back allJourneysBack) {

                        paintActivity.hidProgressDialog();

                        if (allJourneysBack.getRet() == Constant.SUCCESSRESPONSE) {

                            try {
                                paintActivity.uploadSuccess();
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }

                        } else {
                            paintActivity.showError(allJourneysBack.getErr());
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void playPicture(int pictureId) {
        Map<String, Integer> map = new HashMap<>();
        map.put("picture_id", pictureId);
        final Subscription subscription = RetrofitUtils.api()
                .playPic(getBody(map))
                .map(new Func1<Back, Back>() {
                    @Override
                    public Back call(Back back) {
                        return back;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Back>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        paintActivity.hidProgressDialog();
                        paintActivity.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(Back allJourneysBack) {

                        paintActivity.hidProgressDialog();

                        if (allJourneysBack.getRet() == Constant.SUCCESSRESPONSE) {

                            try {
                                paintActivity.uploadSuccess();
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }

                        } else {
                            paintActivity.showError(allJourneysBack.getErr());
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void addPlayLight(int playLight) {
        Map<String, Integer> map = new HashMap<>();
        map.put("play_type", playLight);
        final Subscription subscription = RetrofitUtils.api()
                .modifyPlayType(getBody(map))
                .map(new Func1<Back, Back>() {
                    @Override
                    public Back call(Back back) {
                        return back;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Back>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        paintActivity.hidProgressDialog();
                        paintActivity.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(Back allJourneysBack) {

                        paintActivity.hidProgressDialog();

                        if (allJourneysBack.getRet() == Constant.SUCCESSRESPONSE) {

                            try {
                                paintActivity.addLightModeSuccess();
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }

                        } else {
                            paintActivity.showError(allJourneysBack.getErr());
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void getPaintDetail(int paint_id, int last_id) {

        paintActivity.showProgressDialog();

        Map<String, Integer> map = new HashMap<>();
        map.put("last_id", last_id);

        final Subscription subscription = RetrofitUtils.api()
                .getPaintDetail(paint_id, getBody(map))
                .map(new Func1<PaintBack, PaintBack>() {
                    @Override
                    public PaintBack call(PaintBack paintBack) {
                        return paintBack;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PaintBack>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        paintActivity.hidProgressDialog();
                        paintActivity.showError(e.getMessage());
                    }

                    @Override
                    public void onNext(PaintBack allJourneysBack) {

                        paintActivity.hidProgressDialog();

                        if (allJourneysBack.getRet() == Constant.SUCCESSRESPONSE) {

                            SLogger.d("<<", "------画单详情-->>" + JSON.toJSONString(allJourneysBack));

                            try {
                                allJourneysBack.getPaint_detail().setLast_id(allJourneysBack.getLast_id());
                                paintActivity.getMainData(allJourneysBack.getPaint_detail());

                            } catch (ClassCastException e) {
                                SLogger.d("<<", "异常");
                                e.printStackTrace();
                            }

                        } else {
                            paintActivity.showError(allJourneysBack.getErr());
                        }

                    }
                });

        addSubscription(subscription);
    }
}
