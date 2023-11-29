package my.project.sakuraproject.cling.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.support.model.PositionInfo;

import java.util.Collection;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.sakuraproject.R;
import my.project.sakuraproject.cling.Intents;
import my.project.sakuraproject.cling.adapter.DevicesAdapter;
import my.project.sakuraproject.cling.control.ClingPlayControl;
import my.project.sakuraproject.cling.control.callback.ControlCallback;
import my.project.sakuraproject.cling.control.callback.ControlReceiveCallback;
import my.project.sakuraproject.cling.entity.ClingDevice;
import my.project.sakuraproject.cling.entity.ClingDeviceList;
import my.project.sakuraproject.cling.entity.DLANPlayState;
import my.project.sakuraproject.cling.entity.IDevice;
import my.project.sakuraproject.cling.entity.IResponse;
import my.project.sakuraproject.cling.listener.BrowseRegistryListener;
import my.project.sakuraproject.cling.listener.DeviceListChangedListener;
import my.project.sakuraproject.cling.service.ClingUpnpService;
import my.project.sakuraproject.cling.service.manager.ClingManager;
import my.project.sakuraproject.cling.service.manager.DeviceManager;
import my.project.sakuraproject.cling.util.OtherUtils;
import my.project.sakuraproject.main.base.BaseActivity;
import my.project.sakuraproject.main.base.Presenter;
import my.project.sakuraproject.util.Utils;

public class DLNAActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = DLNAActivity.class.getSimpleName();
    /** 连接设备状态: 播放状态 */
    public static final int PLAY_ACTION = 1;
    /** 连接设备状态: 暂停状态 */
    public static final int PAUSE_ACTION = 2;
    /** 连接设备状态: 停止状态 */
    public static final int STOP_ACTION = 3;
    /** 连接设备状态: 转菊花状态 */
    public static final int TRANSITIONING_ACTION = 4;
    /** 投放失败 */
    public static final int ERROR_ACTION = 5;
    /** 获取进度 */
    public static final int GET_POSITION_INFO_ACTION = 6;

    private Context mContext;
    private Handler mHandler = new InnerHandler();
    @BindView(R.id.video_url)
    TextView videoUrlView;
    @BindView(R.id.lv_devices)
    ListView mDeviceList;
    @BindView(R.id.tv_selected)
    TextView mTVSelected;
    @BindView(R.id.seekbar_progress)
    SeekBar mSeekProgress;
    @BindView(R.id.seekbar_volume)
    SeekBar mSeekVolume;
    @BindView(R.id.duration)
    TextView durationText;
//    private Switch mSwitchMute;
    private boolean isSeek = false;
    private String posTime;
    private String refTimeText = "%s/%s";
    private boolean isMain = true;

    private BroadcastReceiver mTransportStateBroadcastReceiver;
    private ArrayAdapter<ClingDevice> mDevicesAdapter;
    /**
     * 投屏控制器
     */
    private ClingPlayControl mClingPlayControl = new ClingPlayControl();

    /** 用于监听发现设备 */
    private BrowseRegistryListener mBrowseRegistryListener = new BrowseRegistryListener();

    private ServiceConnection mUpnpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "mUpnpServiceConnection onServiceConnected");

            ClingUpnpService.LocalBinder binder = (ClingUpnpService.LocalBinder) service;
            ClingUpnpService beyondUpnpService = binder.getService();

            ClingManager clingUpnpServiceManager = ClingManager.getInstance();
            clingUpnpServiceManager.setUpnpService(beyondUpnpService);
            clingUpnpServiceManager.setDeviceManager(new DeviceManager());

            clingUpnpServiceManager.getRegistry().addListener(mBrowseRegistryListener);
            //Search on service created.
            clingUpnpServiceManager.searchDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "mUpnpServiceConnection onServiceDisconnected");

            ClingManager.getInstance().setUpnpService(null);
        }
    };
    private String playUrl; // 视频播放地址
    private long duration; // 视屏长度

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_dlna;
    }

    @Override
    protected void init() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.6f;
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = (int) Math.round(outMetrics.widthPixels / 1.2);
        attributes.height = (int) Math.round(outMetrics.heightPixels / 1.2);
        getWindow().setAttributes(attributes);
        mContext = this;
        playUrl = getIntent().getExtras().getString("playUrl");
        videoUrlView.setText(String.format(getString(R.string.video_url), playUrl));
        duration = getIntent().getExtras().getLong("duration");
        initView();
        initListeners();
        bindServices();
        registerReceivers();
        hideNavBar();
    }

    @Override
    protected void initBeforeView() {

    }

    private void registerReceivers() {
        //Register play status broadcast
        mTransportStateBroadcastReceiver = new TransportStateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_PLAYING);
        filter.addAction(Intents.ACTION_PAUSED_PLAYBACK);
        filter.addAction(Intents.ACTION_STOPPED);
        filter.addAction(Intents.ACTION_TRANSITIONING);
        registerReceiver(mTransportStateBroadcastReceiver, filter);
    }


    private void bindServices() {
        // Bind UPnP service
        Intent upnpServiceIntent = new Intent(DLNAActivity.this, ClingUpnpService.class);
        bindService(upnpServiceIntent, mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
        // Bind System service
        //        Intent systemServiceIntent = new Intent(MainActivity.this, SystemService.class);
        //        bindService(systemServiceIntent, mSystemServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        // Unbind UPnP service
        unbindService(mUpnpServiceConnection);
        // Unbind System service
        //        unbindService(mSystemServiceConnection);
        // UnRegister Receiver
        unregisterReceiver(mTransportStateBroadcastReceiver);
        postHandler.removeCallbacksAndMessages(null);
        ClingManager.getInstance().destroy();
        ClingDeviceList.getInstance().destroy();
    }

    @Override
    protected void setConfigurationChanged() {

    }

    private void initView() {
        mDevicesAdapter = new DevicesAdapter(mContext);
        mDeviceList.setAdapter(mDevicesAdapter);
        // 片源的时间
        mSeekProgress.setMax((int) duration);
        durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
        // 设置最大音量
        mSeekVolume.setMax(100);
    }

    private void initListeners() {
/*        mRefreshLayout.setOnRefreshListener(() -> {
            mRefreshLayout.setRefreshing(true);
            mDeviceList.setEnabled(false);

            mRefreshLayout.setRefreshing(false);
            refreshDeviceList();
            mDeviceList.setEnabled(true);
        });*/
        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            // 选择连接设备
            ClingDevice item = mDevicesAdapter.getItem(position);
            if (OtherUtils.isNull(item)) {
                return;
            }

            ClingManager.getInstance().setSelectedDevice(item);

            Device device = item.getDevice();
            if (OtherUtils.isNull(device)) {
                return;
            }

            String selectedDeviceName = String.format(getString(R.string.selectedText), device.getDetails().getFriendlyName());
            mTVSelected.setText(selectedDeviceName);
        });

        // 设置发现设备监听
        mBrowseRegistryListener.setOnDeviceListChangedListener(new DeviceListChangedListener() {
            @Override
            public void onDeviceAdded(final IDevice device) {
                runOnUiThread(() -> mDevicesAdapter.add((ClingDevice) device));
            }

            @Override
            public void onDeviceRemoved(final IDevice device) {
                runOnUiThread(() -> mDevicesAdapter.remove((ClingDevice) device));
            }
        });
        // 静音开关
   /*     mSwitchMute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mClingPlayControl.setMute(isChecked, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "setMute success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "setMute fail");
                    }
                });
            }
        });*/

        mSeekProgress.setOnSeekBarChangeListener(this);
        mSeekVolume.setOnSeekBarChangeListener(this);
    }

    /**
     * 刷新设备
     */
    private void refreshDeviceList() {
        Collection<ClingDevice> devices = ClingManager.getInstance().getDmrDevices();
        ClingDeviceList.getInstance().setClingDeviceList(devices);
        if (devices != null) {
            mDevicesAdapter.clear();
            mDevicesAdapter.addAll(devices);
        }
    }

    @OnClick({R.id.play, R.id.pause, R.id.stop, R.id.exit})
    public void onClick(View view) {
        if (!Utils.isFastClick()) return;
        switch (view.getId()) {
            case R.id.play:
                play();
                break;
            case R.id.pause:
                pause();
                break;
            case R.id.stop:
                stop();
                break;
            case R.id.exit:
                finish();
                break;
        }
    }

    /**
     * 停止
     */
    private void stop() {
        mClingPlayControl.stop(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "stop success");
                mHandler.sendEmptyMessage(STOP_ACTION);
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "stop fail");
            }
        });
    }

    /**
     * 暂停
     */
    private void pause() {
        isMain = false;
        mClingPlayControl.pause(new ControlCallback() {
            @Override
            public void success(IResponse response) {
                Log.e(TAG, "pause success");
            }

            @Override
            public void fail(IResponse response) {
                Log.e(TAG, "pause fail");
            }
        });
    }

    /**
     * 播放视频
     */
    private void play() {
        @DLANPlayState.DLANPlayStates int currentState = mClingPlayControl.getCurrentState();

        /**
         * 通过判断状态 来决定 是继续播放 还是重新播放
         */

        if (currentState == DLANPlayState.STOP) {
            isMain = true;
            mSeekProgress.setMax((int) duration);
            durationText.setText(String.format(refTimeText, "00:00:00", OtherUtils.getStringTime((int) duration)));
            mClingPlayControl.playNew(playUrl, new ControlCallback() {

                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                    ClingManager.getInstance().registerAVTransport(mContext);
                    ClingManager.getInstance().registerRenderingControl(mContext);
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        } else {
            mClingPlayControl.play(new ControlCallback() {
                @Override
                public void success(IResponse response) {
                    Log.e(TAG, "play success");
                }

                @Override
                public void fail(IResponse response) {
                    Log.e(TAG, "play fail");
                    mHandler.sendEmptyMessage(ERROR_ACTION);
                }
            });
        }
    }

    /******************* start progress changed listener *************************/

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Start Seek");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(TAG, "Stop Seek");
        int id = seekBar.getId();
        switch (id) {
            case R.id.seekbar_progress: // 进度
                int currentProgress = seekBar.getProgress(); // 转为毫秒
                mClingPlayControl.seek(currentProgress, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        posTime = OtherUtils.getStringTime(currentProgress);
//                        durationText.setText(String.format(refTimeText, posTime, OtherUtils.getStringTime((int) duration)));
                        isSeek = true;
                        Log.e(TAG, "seek success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "seek fail");
                    }
                });
                break;
            case R.id.seekbar_volume:   // 音量
                int currentVolume = seekBar.getProgress();
                mClingPlayControl.setVolume(currentVolume, new ControlCallback() {
                    @Override
                    public void success(IResponse response) {
                        Log.e(TAG, "volume success");
                    }

                    @Override
                    public void fail(IResponse response) {
                        Log.e(TAG, "volume fail");
                    }
                });
                break;
        }
    }

    /******************* end progress changed listener *************************/

    private final class InnerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_ACTION:
                    Log.i(TAG, "Execute PLAY_ACTION");
                    if (isMain) {
                        Toast.makeText(mContext, isSeek ? "快进到：" + posTime : "开始投屏", Toast.LENGTH_SHORT).show();
                    }
                    isSeek = false;
                    mClingPlayControl.setCurrentState(DLANPlayState.PLAY);
                    postHandler.post(positionRunnable);
                    break;
                case PAUSE_ACTION:
                    Log.i(TAG, "Execute PAUSE_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.PAUSE);

                    postHandler.post(positionRunnable);
                    postHandler.removeCallbacksAndMessages(null);
                    break;
                case STOP_ACTION:
                    Log.i(TAG, "Execute STOP_ACTION");
                    mClingPlayControl.setCurrentState(DLANPlayState.STOP);

                    postHandler.post(positionRunnable);
                    postHandler.removeCallbacksAndMessages(null);

                    mSeekProgress.setProgress(0);
                    durationText.setText(String.format(refTimeText, "00:00:00", "00:00:00"));
                    break;
                case TRANSITIONING_ACTION:
                    Log.i(TAG, "Execute TRANSITIONING_ACTION");
                    Toast.makeText(mContext, "正在连接", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_ACTION:
                    Log.e(TAG, "Execute ERROR_ACTION");
                    Toast.makeText(mContext, "投放失败", Toast.LENGTH_SHORT).show();

                    postHandler.post(positionRunnable);
                    postHandler.removeCallbacksAndMessages(null);
                    break;
            }
        }
    }

    /**
     * 接收状态改变信息
     */
    private class TransportStateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "Receive playback intent:" + action);
            if (Intents.ACTION_PLAYING.equals(action)) {
                mHandler.sendEmptyMessage(PLAY_ACTION);

            } else if (Intents.ACTION_PAUSED_PLAYBACK.equals(action)) {
                mHandler.sendEmptyMessage(PAUSE_ACTION);

            } else if (Intents.ACTION_STOPPED.equals(action)) {
                mHandler.sendEmptyMessage(STOP_ACTION);

            } else if (Intents.ACTION_TRANSITIONING.equals(action)) {
                mHandler.sendEmptyMessage(TRANSITIONING_ACTION);
            }
        }
    }
    Handler postHandler = new Handler();
    private int refreshPositionTime = 1000; // 刷新时长
    private Runnable positionRunnable = new Runnable() {
        @Override
        public void run() {
            mClingPlayControl.getPositionInfo(new ControlReceiveCallback() {
                @Override
                public void receive(IResponse response) {
                    if (response != null) {
                        runOnUiThread(() -> {
                            PositionInfo positionInfo = (PositionInfo) response.getResponse();
                            if (OtherUtils.getIntTime(positionInfo.getRelTime()) == 0) return;
                            mSeekProgress.setProgress(OtherUtils.getIntTime(positionInfo.getRelTime()));
                            durationText.setText(String.format(refTimeText, positionInfo.getRelTime(), positionInfo.getTrackDuration()));
                        });
                    }
                }

                @Override
                public void success(IResponse response) {

                }

                @Override
                public void fail(IResponse response) {

                }
            });
            postHandler.postDelayed(this, refreshPositionTime);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        postHandler.post(positionRunnable);

    }

    @Override
    protected void onPause() {
        super.onPause();
        postHandler.removeCallbacksAndMessages(null);
    }
}