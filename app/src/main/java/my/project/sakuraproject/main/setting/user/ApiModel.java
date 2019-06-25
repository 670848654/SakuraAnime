package my.project.sakuraproject.main.setting.user;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.ApiBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.Utils;

public class ApiModel implements ApiContract.Model {

    @Override
    public void getData(ApiContract.LoadDataCallback callback) {
        List<ApiBean> list = DatabaseUtil.queryAllApi();
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.no_api));
    }
}
