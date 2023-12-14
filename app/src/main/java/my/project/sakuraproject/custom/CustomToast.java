package my.project.sakuraproject.custom;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.util.Utils;

/**
 * 自定义提示窗
 */
public class CustomToast {
    private static PopupWindow popupWindow;
    public static final int DEFAULT = 0;
    public static final int ERROR = 1;
    public static final int SUCCESS = 2;
    public static final int WARNING = 3;

    private static String getRunningActivityName(Activity activity) {
        String contextString = activity.toString();
        return contextString.substring(contextString.lastIndexOf(".") + 1,
                contextString.indexOf("@"));
    }

    public static void showToast(final Activity activity, final CharSequence text, int status) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow();
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout linearLayout = new LinearLayout(activity);
            View viewContent = inflater.inflate(R.layout.custom_toast_layout, linearLayout);
            popupWindow.setClippingEnabled(false);
            popupWindow.setContentView(viewContent);
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setAnimationStyle(R.style.PopupTopAnim);
        }
        final TextView tv = popupWindow.getContentView().findViewById(R.id.tv);
        Double top = Utils.getStatusBarHeight() * 1.5;
        tv.setPadding(0, getRunningActivityName(activity).equals("PlayerActivity") ? Utils.getStatusBarHeight() : top.intValue(), 0, Utils.getStatusBarHeight());
        switch (status) {
            case DEFAULT:
                tv.setBackground(activity.getDrawable(R.drawable.toast_style_default));
                break;
            case ERROR:
                tv.setBackground(activity.getDrawable(R.drawable.toast_style_error));
                break;
            case SUCCESS:
                tv.setBackground(activity.getDrawable(R.drawable.toast_style_success));
                break;
            case WARNING:
                tv.setBackground(activity.getDrawable(R.drawable.toast_style_warning));
                break;
        }
        tv.setText(text);
        popupWindow.getContentView().setOnClickListener(v -> {
        });
        handler.removeMessages(1);
        handler.sendEmptyMessageDelayed(1, getRunningActivityName(activity).equals("PlayerActivity") ? 1500 : 3000);
        Sakura.mainHandler.post(() -> popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.TOP, 0, 0));
    }

    public static void dismiss(){
        popupWindow.dismiss();
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            popupWindow.dismiss();
        }
    };

    /**
     * 从dp单位转换为px
     *
     * @param dp dp值
     * @return 返回转换后的px值
     */
    private static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /*public static void showToast(Context context, String message, int status) {
        Sakura.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
                LinearLayout rootView = toastView.findViewById(R.id.root_view);
                switch (status) {
                    case DEFAULT:
                        rootView.setBackground(context.getDrawable(R.drawable.toast_style_default));
                        break;
                    case ERROR:
                        rootView.setBackground(context.getDrawable(R.drawable.toast_style_error));
                        break;
                    case SUCCESS:
                        rootView.setBackground(context.getDrawable(R.drawable.toast_style_success));
                        break;
                    case WARNING:
                        rootView.setBackground(context.getDrawable(R.drawable.toast_style_warning));
                        break;
                }
                mTextView = toastView.findViewById(R.id.message);
                mTextView.setText(message);
                Toast toast = new Toast(context);
                toast.setGravity(Gravity.BOTTOM, 0, 50);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastView);
                toast.show();
            }
        });
    }*/
}
