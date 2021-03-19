package my.project.sakuraproject.cling.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.fourthline.cling.model.meta.Device;

import my.project.sakuraproject.R;
import my.project.sakuraproject.cling.entity.ClingDevice;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/28 15:50
 */

public class DevicesAdapter extends ArrayAdapter<ClingDevice> {
    private Context context;
    private LayoutInflater mInflater;

    public DevicesAdapter(Context context) {
        super(context, 0);
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = mInflater.inflate(R.layout.item_device, null);

        ClingDevice item = getItem(position);
        if (item == null || item.getDevice() == null) {
            return convertView;
        }

        Device device = item.getDevice();

        ImageView imageView = convertView.findViewById(R.id.img);
        imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_color_primary)));

        TextView textView = convertView.findViewById(R.id.title);
        textView.setText(device.getDetails().getFriendlyName());
        textView.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_color_primary)));
        return convertView;
    }
}