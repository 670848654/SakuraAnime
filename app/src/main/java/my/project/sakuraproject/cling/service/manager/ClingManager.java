package my.project.sakuraproject.cling.service.manager;

import android.content.Context;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.Nullable;
import my.project.sakuraproject.cling.entity.ClingControlPoint;
import my.project.sakuraproject.cling.entity.ClingDevice;
import my.project.sakuraproject.cling.entity.IControlPoint;
import my.project.sakuraproject.cling.entity.IDevice;
import my.project.sakuraproject.cling.service.ClingUpnpService;
import my.project.sakuraproject.cling.util.ListUtils;
import my.project.sakuraproject.cling.util.OtherUtils;

/**
 * 说明：所有对服务的操作都通过该类代理执行
 * 作者：zhouzhan
 * 日期：17/6/27 18:12
 */

public class ClingManager implements IClingManager {

//    public static final ServiceType CONTENT_DIRECTORY_SERVICE = new UDAServiceType("ContentDirectory"); AVTransport
    public static final ServiceType AV_TRANSPORT_SERVICE = new UDAServiceType("AVTransport");
    /** 控制服务 */
    public static final ServiceType RENDERING_CONTROL_SERVICE = new UDAServiceType("RenderingControl");
    public static final DeviceType DMR_DEVICE_TYPE = new UDADeviceType("MediaRenderer");

    private static ClingManager INSTANCE = null;

    private ClingUpnpService mUpnpService;
    private IDeviceManager mDeviceManager;

//    private SystemService mSystemService;

    private ClingManager() {
    }

    public static ClingManager getInstance() {
        if (OtherUtils.isNull(INSTANCE)) {
            INSTANCE = new ClingManager();
        }
        return INSTANCE;
    }


    @Override
    public void searchDevices() {
        if (!OtherUtils.isNull(mUpnpService)) {
            mUpnpService.getControlPoint().search();
        }
    }

    @Override
    @Nullable
    public Collection<ClingDevice> getDmrDevices() {
        if (OtherUtils.isNull(mUpnpService)) {
            return null;
        }

        Collection<Device> devices = mUpnpService.getRegistry().getDevices(DMR_DEVICE_TYPE);
        if (ListUtils.isEmpty(devices)) {
            return null;
        }

        Collection<ClingDevice> clingDevices = new ArrayList<>();
        for (Device device : devices) {
            ClingDevice clingDevice = new ClingDevice(device);
            clingDevices.add(clingDevice);
        }
        return clingDevices;
    }

    @Override
    @Nullable
    public IControlPoint getControlPoint() {
        if (OtherUtils.isNull(mUpnpService)) {
            return null;
        }
        ClingControlPoint.getInstance().setControlPoint(mUpnpService.getControlPoint());

        return ClingControlPoint.getInstance();
    }

    @Override
    public Registry getRegistry() {
        return mUpnpService.getRegistry();
    }

    @Override
    public IDevice getSelectedDevice() {
        if (OtherUtils.isNull(mDeviceManager)) {
            return null;
        }
        return mDeviceManager.getSelectedDevice();
    }

    @Override
    public void cleanSelectedDevice() {
        if (OtherUtils.isNull(mDeviceManager)) {
            return;
        }
        mDeviceManager.cleanSelectedDevice();
    }

    @Override
    public void setSelectedDevice(IDevice device) {
        mDeviceManager.setSelectedDevice(device);
    }

    @Override
    public void registerAVTransport(Context context) {
        if (OtherUtils.isNull(mDeviceManager))
            return;
        mDeviceManager.registerAVTransport(context);
    }

    @Override
    public void registerRenderingControl(Context context) {
        if (OtherUtils.isNull(mDeviceManager))
            return;

        mDeviceManager.registerRenderingControl(context);
    }

    @Override
    public void setUpnpService(ClingUpnpService upnpService) {
        mUpnpService = upnpService;
    }

    @Override
    public void setDeviceManager(IDeviceManager deviceManager) {
        mDeviceManager = deviceManager;
    }

    @Override
    public void destroy() {
        mUpnpService.onDestroy();
        mDeviceManager.destroy();
    }
}
