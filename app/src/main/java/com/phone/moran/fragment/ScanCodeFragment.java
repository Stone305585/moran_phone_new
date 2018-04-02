package com.phone.moran.fragment;


import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.phone.moran.R;
import com.phone.moran.event.BindSuccess;
import com.phone.moran.presenter.implPresenter.ScanCodeActivityImpl;
import com.phone.moran.presenter.implView.IScanCodeActivity;
import com.phone.moran.tools.AppUtils;
import com.phone.moran.tools.SLogger;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.CaptureActivityHandler;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

import de.greenrobot.event.EventBus;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanCodeFragment extends BaseFragment  implements SurfaceHolder.Callback, IScanCodeActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View v;//根view

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    private ScanCodeActivityImpl scanCodeActivityImpl;


    public ScanCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanCodeFragment newInstance(String param1, String param2) {
        ScanCodeFragment fragment = new ScanCodeFragment();
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
        scanCodeActivityImpl = new ScanCodeActivityImpl(getActivity(), token, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_scan_code, container, false);

        initView(v);
        setListener();
        return v;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);

        int a = getDisplayRotation(getActivity());

        SLogger.d("<<", "--...>>" + a);
        //初始化view，初始化camera manager
        CameraManager.init(getActivity().getApplication(), a);
        viewfinderView = (ViewfinderView) view.findViewById(R.id.viewfinder_view);
        hasSurface = false;
//        inactivityTimer = new InactivityTimer(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        //初始化摄像页面
        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        //初始化手机振动状态
        AudioManager audioService = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    /**
     * 初始化camera
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    /**
     * 初始化扫描识别的声音
     */
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            SLogger.d("<<", "--camera  close--->>>>>>");
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
//        inactivityTimer.shutdown();
        super.onDestroy();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public void setViewfinderView(ViewfinderView viewfinderView) {
        this.viewfinderView = viewfinderView;
    }

    /**
     * Handler scan result
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        //处理扫描成功的结果
//        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXMEe
        if (resultString.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "Scan failed!", Toast.LENGTH_SHORT).show();
        }else {
			SLogger.d("<<", "Result:"+resultString);
			viewfinderView.setVisibility(View.GONE);
			scanCodeActivityImpl.bind(resultString);
            //显示结果
//            resultText.setText(resultString);
        }
    }

    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void setHandler(CaptureActivityHandler handler) {
        this.handler = handler;
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
        AppUtils.showToast(getActivity().getApplicationContext(), error);
    }


    @Override
    public void bandFinish() {
        EventBus.getDefault().post(new BindSuccess());

        AppUtils.showToast(getActivity().getApplicationContext(), getResources().getString(R.string.bind_success));
    }

    public int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
}
