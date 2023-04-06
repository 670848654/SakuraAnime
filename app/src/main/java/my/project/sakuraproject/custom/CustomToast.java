package my.project.sakuraproject.custom;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import my.project.sakuraproject.R;
import my.project.sakuraproject.application.Sakura;

public class CustomToast {
    private static TextView mTextView;
    public static final int DEFAULT = 0;
    public static final int ERROR = 1;
    public static final int SUCCESS = 2;
    public static final int WARNING = 3;
    public static void showToast(Context context, String message, int status) {
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
    }
}
