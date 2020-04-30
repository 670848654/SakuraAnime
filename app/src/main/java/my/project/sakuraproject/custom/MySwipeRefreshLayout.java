package my.project.sakuraproject.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    private float startX;
    private float startY;
    private float mTouchSlop;

    public MySwipeRefreshLayout(Context context) {
        super(context);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = Math.abs(ev.getX() - startX);
                float distanceY = Math.abs(ev.getY() - startY);
                if(distanceX > mTouchSlop && distanceX > distanceY){  //判断为横向滑动
                    return false;
                }

                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
