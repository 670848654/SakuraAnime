package my.project.sakuraproject.cling.control;

import android.content.Context;

import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.meta.Device;

import androidx.annotation.NonNull;
import my.project.sakuraproject.cling.entity.IDevice;
import my.project.sakuraproject.cling.service.callback.AVTransportSubscriptionCallback;
import my.project.sakuraproject.cling.service.callback.RenderingControlSubscriptionCallback;
import my.project.sakuraproject.cling.service.manager.ClingManager;
import my.project.sakuraproject.cling.util.ClingUtils;
import my.project.sakuraproject.cling.util.OtherUtils;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/7/21 16:43
 */

public class SubscriptionControl implements ISubscriptionControl<Device> {

    private AVTransportSubscriptionCallback mAVTransportSubscriptionCallback;
    private RenderingControlSubscriptionCallback mRenderingControlSubscriptionCallback;

    public SubscriptionControl() {
    }

    @Override
    public void registerAVTransport(@NonNull IDevice<Device> device, @NonNull Context context) {
        if (OtherUtils.isNotNull(mAVTransportSubscriptionCallback)) {
            mAVTransportSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = ClingUtils.getControlPoint();
        if (OtherUtils.isNull(controlPointImpl)) {
            return;
        }

        mAVTransportSubscriptionCallback = new AVTransportSubscriptionCallback(device.getDevice().findService(ClingManager.AV_TRANSPORT_SERVICE), context);
        controlPointImpl.execute(mAVTransportSubscriptionCallback);
    }

    @Override
    public void registerRenderingControl(@NonNull IDevice<Device> device, @NonNull Context context) {
        if (OtherUtils.isNotNull(mRenderingControlSubscriptionCallback)) {
            mRenderingControlSubscriptionCallback.end();
        }
        final ControlPoint controlPointImpl = ClingUtils.getControlPoint();
        if (OtherUtils.isNull(controlPointImpl)) {
            return;
        }
        mRenderingControlSubscriptionCallback = new RenderingControlSubscriptionCallback(device.getDevice().findService(ClingManager
                .RENDERING_CONTROL_SERVICE), context);
        controlPointImpl.execute(mRenderingControlSubscriptionCallback);
    }

    @Override
    public void destroy() {
        if (OtherUtils.isNotNull(mAVTransportSubscriptionCallback)) {
            mAVTransportSubscriptionCallback.end();
        }
        if (OtherUtils.isNotNull(mRenderingControlSubscriptionCallback)) {
            mRenderingControlSubscriptionCallback.end();
        }
    }
}
