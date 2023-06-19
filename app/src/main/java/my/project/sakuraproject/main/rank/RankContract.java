package my.project.sakuraproject.main.rank;

import java.util.List;

import my.project.sakuraproject.bean.SiliSiliRankBean;
import my.project.sakuraproject.main.base.BaseLoadDataCallback;
import my.project.sakuraproject.main.base.BaseView;

public interface RankContract {
    interface Model {
        void getData(LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showSuccess(List<SiliSiliRankBean> siliSiliRankBeans);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(List<SiliSiliRankBean> siliSiliRankBeans);
    }
}
