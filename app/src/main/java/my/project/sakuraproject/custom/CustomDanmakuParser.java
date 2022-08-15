package my.project.sakuraproject.custom;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.android.JSONSource;

public class CustomDanmakuParser extends BaseDanmakuParser {
    @Override
    protected IDanmakus parse() {
        if (mDataSource != null && mDataSource instanceof JSONSource) {
            JSONSource jsonSource = (JSONSource) mDataSource;
            return doParse(jsonSource.data());
        }
        return new Danmakus();
    }

    /**
     * @param danmakuDara 弹幕数据
     * @return
     */
    private Danmakus doParse(JSONArray danmakuDara) {
        Danmakus danmakus = new Danmakus();
        if (danmakuDara == null || danmakuDara.length() == 0) {
            return danmakus;
        }
        danmakus = _parse(danmakuDara, danmakus);
        return danmakus;
    }

    private Danmakus _parse(JSONArray jsonArray, Danmakus danmakus) {
        if (danmakus == null) {
            danmakus = new Danmakus();
        }
        try {
            if (jsonArray == null || jsonArray.length() == 0) {
                return danmakus;
            }
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject danmuObj = jsonArray.getJSONObject(i);
                long time = (long) (Float.parseFloat(danmuObj.getString("time")) * 1000); // 出现时间
                int color = Color.WHITE;
                try {
                    color = Color.parseColor(danmuObj.getString("color"));
                } catch (Exception e) {

                }
                int danmuType = BaseDanmaku.TYPE_SCROLL_RL;
                switch (danmuObj.getString("type")) {
                    case "scroll":
                        // 滚动
                        danmuType = BaseDanmaku.TYPE_SCROLL_RL;
                        break;
                    case "top":
                        // 顶部
                        danmuType = BaseDanmaku.TYPE_FIX_TOP;
                        break;
                }
                BaseDanmaku item = mContext.mDanmakuFactory.createDanmaku(danmuType, mContext);
                if (item != null) {
                    item.setTime(time + 1200);
                    item.textColor = color;
                    item.textShadowColor = color <= Color.BLACK ? Color.WHITE : Color.BLACK;
                    item.index = i;
                    item.flags = mContext.mGlobalFlagValues;
                    item.setTimer(mTimer);
                    item.text = danmuObj.getString("content");
//                    item.textShadowColor = Color.GRAY;
                    item.underlineColor = Color.TRANSPARENT;
                    item.borderColor = Color.TRANSPARENT;
                    item.priority = 0;
//                    item.textSize = Utils.dpToPx(Utils.getContext(), 14);
                    item.textSize = 20 * (mDispDensity - 0.6f);
                    danmakus.addItem(item);
                }
            }
        } catch (JSONException e) {
        } catch (NumberFormatException e) {
        }
        return danmakus;
    }
}
