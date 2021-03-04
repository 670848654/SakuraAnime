package my.project.sakuraproject.cling.service.manager;


import org.fourthline.cling.registry.Registry;

import my.project.sakuraproject.cling.service.ClingUpnpService;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 16:30
 */

public interface IClingManager extends IDLNAManager {

    void setUpnpService(ClingUpnpService upnpService);

    void setDeviceManager(IDeviceManager deviceManager);

    Registry getRegistry();
}
