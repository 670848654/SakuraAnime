package my.project.sakuraproject.main.favorite;

import java.util.List;

import my.project.sakuraproject.R;
import my.project.sakuraproject.bean.AnimeListBean;
import my.project.sakuraproject.database.DatabaseUtil;
import my.project.sakuraproject.util.Utils;

public class FavoriteModel implements FavoriteContract.Model {

    @Override
    public void getData(FavoriteContract.LoadDataCallback callback) {
        List<AnimeListBean> list = DatabaseUtil.queryAllFavorite();
        if (list.size() > 0)
            callback.success(list);
        else
            callback.error(Utils.getString(R.string.empty_favorite));
    }
}
