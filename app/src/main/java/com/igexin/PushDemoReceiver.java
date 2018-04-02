package com.igexin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.phone.moran.model.PushBack;
import com.phone.moran.tools.SLogger;

public class PushDemoReceiver extends BroadcastReceiver {

    /**
     * 应用未启动, 个推 service已经被唤醒,保存在该时间段内离线消息(此时 GetuiSdkDemoActivity.tLogView == null)
     */

    public static StringBuilder payloadData = new StringBuilder();
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (builder == null)
            this.builder = new NotificationCompat.Builder(context);
        if (notificationManager == null)
            this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mContext == null)
            this.mContext = context;
        Bundle bundle = intent.getExtras();
        SLogger.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                byte[] payload1 = bundle.getByteArray("payload");

                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
                SLogger.d("GetuiSdkDemo", "onReceive() action===>>1");
//
                if (payload1 != null) {
                    String data1 = new String(payload1);
                    PushBack pushData = JSON.parseObject(data1, PushBack.class);
                    SLogger.d("<<", "-->>" + data1);

                    toActivity(pushData);
                }

                break;

            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                String cid = bundle.getString("clientid");
                break;

            case PushConsts.THIRDPART_FEEDBACK:


                break;

            default:
                break;
        }

    }


    /**
     * 跳转到相应的页面
     * //1:播放2： 3：播放模式  4：tips  5：内衬
     *
     * @param pushBack
     */
    private void toActivity(PushBack pushBack) {
//        switch (pushBack.getOpr_type()) {
//            case PushBack.PLAY:
//                int paint_id = PreferencesUtils.getInt(mContext, MainActivity.PAINT_ID);
//
//                Intent intentToPlay = new Intent(mContext.getApplicationContext(), LiningActivity.class);
//                PlayPictureBack pl = new PlayPictureBack();
//                pl.setTitle_info(pushBack.getTitle_info());
//                pl.setPictures(pushBack.getPictures());
//                pl.setPaint_id(++paint_id);
//                PreferencesUtils.putInt(mContext, MainActivity.PAINT_ID, paint_id);
//
//
//                intentToPlay.putExtra(MainActivity.PAINT, pl);
//                intentToPlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intentToPlay);
//                break;
//            case PushBack.MODE:
//                break;
//
//                //TODO 内衬，先搞这个
//            case PushBack.LINING:
//                PreferencesUtils.putInt(mContext, LiningActivity.FRAME_COLOR, pushBack.getFrame_colour());
//                PreferencesUtils.putInt(mContext, LiningActivity.FRAME_SIZE, pushBack.getFrame_size());
//                EventBus.getDefault().post(new LiningEvent());
////                Intent intentToLining = new Intent(mContext.getApplicationContext(), LiningActivity.class);
//                /*LiningBack liningBack = new LiningBack();
//                liningBack.setFrame_colour(pushBack.getFrame_colour());
//                liningBack.setFrame_size(pushBack.getFrame_size());*/
////                intentToLining.putExtra(LiningActivity.FRAME_SIZE, pushBack.getFrame_size());
////                intentToLining.putExtra(LiningActivity.FRAME_COLOR, pushBack.getFrame_colour());
////                intentToLining.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                mContext.startActivity(intentToLining);
//                break;
//            case PushBack.TIPS:
//                Intent intentToTips = new Intent(mContext.getApplicationContext(), TipsActivity.class);
//                TipsBack tipsBack = new TipsBack();
//                tipsBack.setTips_content(pushBack.getTips_content());
//                tipsBack.setTips_location(pushBack.getTips_location());
//                tipsBack.setTips_texure(pushBack.getTips_texure());
//                tipsBack.setFlag(pushBack.getFlag());
//                intentToTips.putExtra(TipsActivity.POSITION, tipsBack);
//                intentToTips.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intentToTips);
//                break;
//
//                case PushBack.PICTURE:
//                    Intent intentToLining = new Intent(mContext.getApplicationContext(), LiningActivity.class);
//                /*LiningBack liningBack = new LiningBack();
//                liningBack.setFrame_colour(pushBack.getFrame_colour());
//                liningBack.setFrame_size(pushBack.getFrame_size());*/
////                    intentToLining.putExtra(LiningActivity.FRAME_SIZE, pushBack.getFrame_size());
////                    intentToLining.putExtra(LiningActivity.FRAME_COLOR, pushBack.getFrame_colour());
//                    Picture picture = pushBack.getPicture_detail();
//                    intentToLining.putExtra(LiningActivity.PICTURE, picture);
//                    intentToLining.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(intentToLining);
//                    break;
//        }
    }


}
