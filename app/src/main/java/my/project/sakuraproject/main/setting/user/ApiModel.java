package my.project.sakuraproject.main.setting.user;

import java.util.List;

import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.database.DatabaseUtil;

public class ApiModel implements ApiContract.Model {

    @Override
    public void getData(ApiContract.LoadDataCallback callback) {
        List<ApiBean> list = DatabaseUtil.queryAllApi();
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error("未自定义解析Api~");
    }
}
