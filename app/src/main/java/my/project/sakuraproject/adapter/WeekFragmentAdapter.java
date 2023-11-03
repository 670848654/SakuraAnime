package my.project.sakuraproject.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.HashMap;

import my.project.sakuraproject.R;
import my.project.sakuraproject.main.week.WeekFragment;
import my.project.sakuraproject.util.Utils;

public class WeekFragmentAdapter extends FragmentStatePagerAdapter {
    private static final String[] TABS = Utils.getArray(R.array.week_array);
    private int num;
    private HashMap<Integer, Fragment> mFragmentHashMap = new HashMap<>();

    public WeekFragmentAdapter(FragmentManager fm, int num) {
        super(fm);
        this.num = num;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return createFragment(position);
    }

    @Override
    public int getCount() {
        return num;
    }

    private Fragment createFragment(int pos) {
        Fragment fragment = mFragmentHashMap.get(pos);

        if (fragment == null) {
            switch (pos) {
                case 0:
                    fragment = new WeekFragment(TABS[0]);
                    break;
                case 1:
                    fragment = new WeekFragment(TABS[1]);
                    break;
                case 2:
                    fragment = new WeekFragment(TABS[2]);
                    break;
                case 3:
                    fragment = new WeekFragment(TABS[3]);
                    break;
                case 4:
                    fragment = new WeekFragment(TABS[4]);
                    break;
                case 5:
                    fragment = new WeekFragment(TABS[5]);
                    break;
                case 6:
                    fragment = new WeekFragment(TABS[6]);
                    break;
            }
            mFragmentHashMap.put(pos, fragment);
        }
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
    }
}
