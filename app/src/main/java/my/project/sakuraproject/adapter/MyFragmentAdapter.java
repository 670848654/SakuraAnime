package my.project.sakuraproject.adapter;

import android.view.ViewGroup;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import my.project.sakuraproject.main.my.fragment.DownloadFragment;
import my.project.sakuraproject.main.my.fragment.FavoriteFragment;
import my.project.sakuraproject.main.my.fragment.HistoryFragment;

public class MyFragmentAdapter extends FragmentStatePagerAdapter {
    private int num;
    private HashMap<Integer, Fragment> mFragmentHashMap = new HashMap<>();

    public MyFragmentAdapter(FragmentManager fm, int num) {
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
                /*
                case 0:
                    fragment = new FavoriteChannelFragment();
                    break;
                 */
                case 0:
                    fragment = new FavoriteFragment();
                    break;
                case 1:
                    fragment = new HistoryFragment();
                    break;
                case 2:
                    fragment = new DownloadFragment();
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
