package my.project.sakuraproject.main.week;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.project.sakuraproject.R;
import my.project.sakuraproject.adapter.WeekAdapter;
import my.project.sakuraproject.application.Sakura;
import my.project.sakuraproject.bean.HomeWekBean;
import my.project.sakuraproject.main.base.LazyFragment;
import my.project.sakuraproject.main.desc.DescActivity;
import my.project.sakuraproject.util.Utils;

@SuppressLint("ValidFragment")
public class WeekFragment extends LazyFragment {
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    @BindView(R.id.loading)
    ProgressBar loading;
    protected WeekAdapter adapter;
    private List<HomeWekBean> list = new ArrayList<>();
    private Sakura application;
    private View view;
    private View errorView;
    private TextView errorTitle;
    private String week;
    private Unbinder mUnBinder;

    public WeekFragment(String week) {
        this.week = week;
    }

    @Override
    protected View initViews(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_week, container, false);
            mUnBinder = ButterKnife.bind(this, view);
        } else {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        errorView = getLayoutInflater().inflate(R.layout.base_error_view, (ViewGroup) recyclerView.getParent(), false);
        errorTitle = errorView.findViewById(R.id.title);
        if (Utils.checkHasNavigationBar(getActivity())) recyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(getActivity()));
        if (application == null) application = (Sakura) getActivity().getApplication();
        initAdapter();
        return view;
    }

    @Override
    protected void initData() {
        initWeekData();
    }

    public void initAdapter() {
        if (adapter == null) {
//            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Utils.isPad() ? 4 : 2));
            adapter = new WeekAdapter(getActivity(), list);
            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
            adapter.setOnItemClickListener((adapter, view, position) -> {
                if (!Utils.isFastClick()) return;
                HomeWekBean bean = (HomeWekBean) adapter.getItem(position);
                Bundle bundle = new Bundle();
                bundle.putString("name", bean.getTitle());
                bundle.putString("url", bean.getUrl());
                startActivity(new Intent(getActivity(), DescActivity.class).putExtras(bundle));
            });
//            adapter.setOnItemChildClickListener((adapter, view, position) -> {
//                switch (view.getId()) {
//                    case R.id.drama:
//                        HomeWekBean bean = (HomeWekBean) adapter.getItem(position);
//                        Sakura.getInstance().showToastMsg(bean.getDrama());
//                        break;
//                }
//            });
            recyclerView.setAdapter(adapter);
            setRecyclerViewView();
        }
    }

    private void initWeekData() {
        loading.setVisibility(View.GONE);
        if (adapter.getData().isEmpty()) {
            list = getList(week);
            if (list.size() == 0) {
                if (!application.error.isEmpty()) {
                    setRecyclerViewEmpty();
                    errorTitle.setText(application.error);
                    adapter.setEmptyView(errorView);
                }
            } else
                adapter.setNewData(list);
        }
    }

    private List getList(String week) {
        list = new ArrayList<>();
        if (application.week.length() > 0) {
            try {
                JSONArray arr = new JSONArray(application.week.getString(week));
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject object = new JSONObject(arr.getString(i));
                    list.add(new HomeWekBean(object.getString("title"),
                                    object.getString("url"),
                                    object.getString("drama"),
                                    object.getString("dramaUrl")
                            )
                    );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        setRecyclerViewView();
    }

    @Override
    protected void setConfigurationChanged() {
        setRecyclerViewView();
    }

    private void setRecyclerViewEmpty() {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
    }

    private void setRecyclerViewView() {
        position = recyclerView.getLayoutManager() == null ? 0 : ((GridLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        String config = getActivity().getResources().getConfiguration().toString();
        boolean isInMagicWindow = config.contains("miui-magic-windows");
        if (!Utils.isPad())
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 2 : 3));
        else {
            if (isInMagicWindow)
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            else
                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), isPortrait ? 4 : 6));
        }
        recyclerView.getLayoutManager().scrollToPosition(position);
    }
}
