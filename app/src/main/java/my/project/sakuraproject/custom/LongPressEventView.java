package my.project.sakuraproject.custom;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class LongPressEventView extends FrameLayout {
    private LongPressEventListener longPressEventListener;
    private boolean isScroll = false;
    private float downX;
    private float downY;

    public LongPressEventView(Context context) {
        super(context);
        initView();
    }

    public LongPressEventView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LongPressEventView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
    }

    public void setLongPressEventListener(LongPressEventListener longPressEventListener) {
        this.longPressEventListener = longPressEventListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isScroll = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            handler.sendEmptyMessageDelayed(1, 500);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // 放开处理
            handler.removeMessages(1);
            handler.sendEmptyMessage(2);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(downX - event.getX()) > 20 || Math.abs(downY - event.getY()) > 20) {
                //移动
                isScroll = true;
                handler.removeMessages(1);
            }
        }
        return super.onTouchEvent(event);
    }

    // 长按触发handle
    final Handler handler = new Handler(msg -> {
        if (isScroll) {
            return false;
        }
        if (msg.what == 1) {
            // 长按处理
            if (longPressEventListener != null) {
                longPressEventListener.onLongClick(this);
            }
        } else if (msg.what == 2) {
            // 取消长按了
            if (longPressEventListener != null) {
                longPressEventListener.onDisLongClick(this);
            }
        }
        return false;
    });


    public interface LongPressEventListener {

        /**
         * 长按监听
         *
         * @param v v
         */
        void onLongClick(View v);

        /**
         * 取消长按监听
         *
         * @param v v
         */
        void onDisLongClick(View v);
    }
}
