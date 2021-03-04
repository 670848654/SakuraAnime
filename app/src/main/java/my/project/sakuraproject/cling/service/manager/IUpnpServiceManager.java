package my.project.sakuraproject.cling.service.manager;

import java.util.Collection;

import my.project.sakuraproject.cling.entity.IControlPoint;
import my.project.sakuraproject.cling.entity.IDevice;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/27 17:41
 */

public interface IUpnpServiceManager {

    /**
     * 搜索所有的设备
     */
    void searchDevices();

    /**
     * 获取支持 Media 类型的设备
     *
     * @return  设备列表
     */
    Collection<? extends IDevice> getDmrDevices();

    /**
     * 获取控制点
     *
     * @return  控制点
     */
    IControlPoint getControlPoint();

    /**
     * 获取选中的设备
     *
     * @return  选中的设备
     */
    IDevice getSelectedDevice();

    /**
     * 设置选中的设备
     * @param device    已选中设备
     */
    void setSelectedDevice(IDevice device);

    /**
     * 销毁
     */
    void destroy();
}
