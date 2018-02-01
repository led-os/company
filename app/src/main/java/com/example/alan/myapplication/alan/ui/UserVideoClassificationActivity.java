package com.example.alan.myapplication.alan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.example.alan.myapplication.R;
import com.example.alan.myapplication.alan.adapter.vp.recycler.RecyclerItemFormUserVideoClassificationAdapter;
import com.example.alan.myapplication.alan.adapter.vp.recycler.RecyclerItemHistoryUserVideoClassificationAdapter;
import com.example.alan.myapplication.alan.adapter.vp.recycler.RecyclerItemVideoUserVideoClassificationAdapter;
import com.example.alan.myapplication.alan.bean.UserVideoCassificationFormBean;
import com.example.alan.myapplication.alan.bean.UserVideoClassificationVideoBean;
import com.example.alan.myapplication.alan.bean.UserVideoPlayHistoryBean;
import com.example.alan.myapplication.alan.constants.AppUrl;
import com.example.alan.myapplication.alan.http.HttpLoadStateUtil;
import com.example.alan.myapplication.alan.http.HttpManager;
import com.example.alan.myapplication.alan.http.ServerCallBack;
import com.example.alan.myapplication.alan.listener.OnControlSqlFinishListener;
import com.example.alan.myapplication.alan.utils.AllUtils;
import com.example.alan.myapplication.alan.utils.SqlUtils;
import com.zhy.autolayout.AutoLayoutActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Alan on 2018/1/31.
 * 功能：我的影视详情，收藏的影视、片单，观影记录
 */

public class UserVideoClassificationActivity extends AutoLayoutActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    /**
     * 根据传入的Type判断展示 片单，影视，影视记录
     */
    public String mType = "1";
    /**
     * 收藏的片单类型
     */
    public String type_collection_form = "0";
    /**
     * 收藏的影视类型
     */
    public String type_collection_video = "1";
    /**
     * 观影历史
     */
    public String type_video_hisory = "2";
    @Bind(R.id.recycler_view_user_video_classification_activity)
    RecyclerView mRecyclerViewUserVideoClassificationActivity;
    @Bind(R.id.sr_layout_usr_video_classification_activity)
    SwipeRefreshLayout mSrLayoutUsrVideoClassificationActivity;
    public int USER_ID = 13299;
    @Bind(R.id.iv_return_title_bar)
    ImageView mIvReturnTitleBar;
    @Bind(R.id.tv_delete_title_bar)
    TextView mTvDeleteTitleBar;
    @Bind(R.id.tv_choose_title_bar)
    TextView mTvChooseTitleBar;
    @Bind(R.id.tv_title_title_bar)
    TextView mTvTitleTitleBar;
    @Bind(R.id.tv_choose_all_title_bar)
    TextView mTvChooseAllTitleBar;
    @Bind(R.id.ll_root_title_bar)
    LinearLayout mLlRootTitleBar;
    /**
     * 收藏的表单的数据
     */
    private List<UserVideoCassificationFormBean.DataBean.VideoListBean> mFormList = new ArrayList<>();
    private RecyclerItemFormUserVideoClassificationAdapter mFormAdapter;
    /**
     * 收藏的影视的数据
     */
    private List<UserVideoClassificationVideoBean.DataBean.VideoBean> mVideoList = new ArrayList<>();
    private RecyclerItemVideoUserVideoClassificationAdapter mVideoAdapter;
    /**
     * 观影记录
     */
    private List<UserVideoPlayHistoryBean> mHistoryList = new ArrayList<>();
    private RecyclerItemHistoryUserVideoClassificationAdapter mHistoryAdapter;
    private LinearLayoutManager mLayoutManger;
    private BaseQuickAdapter mAdapter;
    private int mCurrPage = 1;
    private String mPageSize = "7";
    /**
     * 是否正在加载
     */
    private boolean isLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_video_classification);
        ButterKnife.bind(this);
//        initIntent();
        initTopBar();
        initSwiRefreshLayout();
        initView();
        initData(false);

    }

    private void initTopBar() {
        mTvChooseTitleBar.setVisibility(View.VISIBLE);
    }

    private void initSwiRefreshLayout() {
        mSrLayoutUsrVideoClassificationActivity.setColorSchemeResources(R.color.swiperefresh_color1, R.color.swiperefresh_color2, R.color.swiperefresh_color3, R.color.swiperefresh_color4);
        mSrLayoutUsrVideoClassificationActivity.setOnRefreshListener(this);
        mSrLayoutUsrVideoClassificationActivity.post(new Runnable() {
            @Override
            public void run() {
                mSrLayoutUsrVideoClassificationActivity.setRefreshing(true);
            }
        });//进入刷新

    }


    private void initData(boolean isLoadMore) {
        switch (mType) {
            case "0"://片单
                initFormData(isLoadMore);
                break;
            case "1"://影视
                initVideoData(isLoadMore);
                break;
            case "2"://历史记录
                initHisoryData();
                break;
        }
    }

    private void initHisoryData() {

        SqlUtils.getInstance().setOnControlSqlFinishListener(new OnControlSqlFinishListener() {
            @Override
            public void onQuerySqlFinishListener(Object obj) {
                mHistoryList = (List<UserVideoPlayHistoryBean>) obj;
                mHistoryAdapter = (RecyclerItemHistoryUserVideoClassificationAdapter) mAdapter;
                if (mHistoryList != null && mHistoryList.size() > 0) {
                    mHistoryAdapter.setNewData(mHistoryList);
                } else {
                    HttpLoadStateUtil.getInstance().loadSateChange(false);
                }
                mSrLayoutUsrVideoClassificationActivity.post(new Runnable() {
                    @Override
                    public void run() {
                        mSrLayoutUsrVideoClassificationActivity.setRefreshing(false);
                    }
                });
            }
        });

        SqlUtils.getInstance().queryVideoPlayHistory2Show(this);

    }


    /**
     * 加载收藏的影视
     *
     * @param isLoadMore
     */
    private void initVideoData(final boolean isLoadMore) {
        LinkedHashMap<String, String> paramas = setHtttpParamas(isLoadMore);
        HttpManager.getInstance().getCallWithParamas(AppUrl.VIDEO_USER_VIDEO_VIDEO, paramas, new ServerCallBack() {
            @Override
            public void responseSucessful(String json) {
                loadFinished();
                UserVideoClassificationVideoBean videoList = HttpManager.getInstance().getGson().fromJson(json, UserVideoClassificationVideoBean.class);
                if (videoList != null) {
                    if (videoList.data != null && videoList.data.video != null && videoList.data.video.size() > 0) {
                        List<UserVideoClassificationVideoBean.DataBean.VideoBean> video = videoList.data.video;
                        mVideoAdapter = (RecyclerItemVideoUserVideoClassificationAdapter) mAdapter;
                        if (!isLoadMore) {
                            mVideoList = video;
                            mVideoAdapter.setNewData(mVideoList);
                        } else {
                            mVideoList.addAll(video);
                            mVideoAdapter.addData(video);
                            mVideoAdapter.loadMoreComplete();
                        }

                    } else {
                        if (isLoadMore) {
                            mVideoAdapter.loadMoreEnd();
                        } else {
                            HttpLoadStateUtil.getInstance().loadSateChange(false);
                        }
                    }

                } else {
                    HttpLoadStateUtil.getInstance().loadSateChange(false);
                }
            }

            @Override
            public void responseClientFailure(String json, int code) {
                HttpLoadStateUtil.getInstance().loadSateChange(false);
                loadFinished();
                if (isLoadMore) {
                    mCurrPage--;
                }
                mFormAdapter.loadMoreFail();
            }

            @Override
            public void responseServerFailure(String json, int code) {
                HttpLoadStateUtil.getInstance().loadSateChange(false);
                loadFinished();
                mFormAdapter.loadMoreFail();
                if (isLoadMore) {
                    mCurrPage--;
                }
            }

            @Override
            public void netWorkFailure(String error) {
                HttpLoadStateUtil.getInstance().loadSateChange(true);
                loadFinished();
                mFormAdapter.loadMoreFail();
                if (isLoadMore) {
                    mCurrPage--;
                }
            }
        }, this);
    }


    /**
     * 加载收藏的片单
     *
     * @param isLoadMore
     */
    private void initFormData(final boolean isLoadMore) {
        LinkedHashMap<String, String> paramas = setHtttpParamas(isLoadMore);
        HttpManager.getInstance().getCallWithParamas(AppUrl.VIDEO_USER_VIDEO_FORM, paramas, new ServerCallBack() {
            @Override
            public void responseSucessful(String json) {
                loadFinished();
                UserVideoCassificationFormBean formList = HttpManager.getInstance().getGson().fromJson(json, UserVideoCassificationFormBean.class);
                if (formList != null) {
                    if (formList.data != null && formList.data.video_list != null && formList.data.video_list.size() > 0) {
                        List<UserVideoCassificationFormBean.DataBean.VideoListBean> video_list = formList.data.video_list;
                        mFormAdapter = (RecyclerItemFormUserVideoClassificationAdapter) mAdapter;
                        if (!isLoadMore) {
                            mFormList = video_list;
                            mFormAdapter.setNewData(mFormList);
                        } else {
                            mFormList.addAll(video_list);
                            mFormAdapter.addData(video_list);
                            mFormAdapter.loadMoreComplete();

                        }

                    } else {
                        if (isLoadMore) {
                            mFormAdapter.loadMoreEnd();
                        } else {
                            HttpLoadStateUtil.getInstance().loadSateChange(false);
                        }
                    }

                } else {
                    HttpLoadStateUtil.getInstance().loadSateChange(false);
                }
            }

            @Override
            public void responseClientFailure(String json, int code) {
                HttpLoadStateUtil.getInstance().loadSateChange(false);
                loadFinished();
                if (isLoadMore) {
                    mCurrPage--;
                }
                mFormAdapter.loadMoreFail();
            }

            @Override
            public void responseServerFailure(String json, int code) {
                HttpLoadStateUtil.getInstance().loadSateChange(false);
                loadFinished();
                mFormAdapter.loadMoreFail();
                if (isLoadMore) {
                    mCurrPage--;
                }
            }

            @Override
            public void netWorkFailure(String error) {
                HttpLoadStateUtil.getInstance().loadSateChange(true);
                loadFinished();
                mFormAdapter.loadMoreFail();
                if (isLoadMore) {
                    mCurrPage--;
                }
            }
        }, this);
    }

    /**
     * 设置请求参数
     *
     * @param isLoadMore 是否是加载更多
     * @return
     */
    @NonNull
    private LinkedHashMap<String, String> setHtttpParamas(boolean isLoadMore) {
        LinkedHashMap<String, String> paramas = new LinkedHashMap<>();
        paramas.put("user_id", USER_ID + "");
        if (isLoadMore) {
            mCurrPage++;
        }
        paramas.put("page", mCurrPage + "");
        paramas.put("page_size", mPageSize + "");
        return paramas;
    }

    private void loadFinished() {
        isLoading = false;
        mSrLayoutUsrVideoClassificationActivity.setRefreshing(false);
    }


    private void initView() {

        switch (mType) {
            case "0"://片单
                initForm();
                break;
            case "1"://影视
                initVideo();
                break;
            case "2"://历史记录
                initHisory();
                break;
        }
        initRecyclerView();
    }


    private void initRecyclerView() {
        mRecyclerViewUserVideoClassificationActivity.setLayoutManager(mLayoutManger);
        mRecyclerViewUserVideoClassificationActivity.getItemAnimator().setChangeDuration(0);
        mRecyclerViewUserVideoClassificationActivity.setAdapter(mAdapter);
        mAdapter.setOnLoadMoreListener(this, mRecyclerViewUserVideoClassificationActivity);

    }

    private void initHisory() {
        mHistoryAdapter = new RecyclerItemHistoryUserVideoClassificationAdapter(R.layout.recycler_item_history, mHistoryList);
        mHistoryAdapter.setContext(this);
        mHistoryAdapter.setEnableLoadMore(false);
        mHistoryAdapter.setHasStableIds(true);
        mHistoryAdapter.setEmptyView(HttpLoadStateUtil.getInstance().setContextAndInitView(this));
        mAdapter = mHistoryAdapter;
        LinearLayoutManager historyLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManger = historyLayoutManager;
    }

    private void initForm() {
        mFormAdapter = new RecyclerItemFormUserVideoClassificationAdapter(R.layout.recycler_item_form_video_fragment, mFormList);
        mFormAdapter.setContext(this);
        mFormAdapter.setEnableLoadMore(true);
        mFormAdapter.setHasStableIds(true);

        mFormAdapter.setEmptyView(HttpLoadStateUtil.getInstance().setContextAndInitView(this));
        mAdapter = mFormAdapter;
        LinearLayoutManager formLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManger = formLayoutManager;
    }

    private void initVideo() {
        mVideoAdapter = new RecyclerItemVideoUserVideoClassificationAdapter(R.layout.recycler_foot_item_detail_video_fragment, mVideoList);
        mVideoAdapter.setContext(this);
        mVideoAdapter.setEnableLoadMore(true);
        mVideoAdapter.setHasStableIds(true);
        mVideoAdapter.setEmptyView(HttpLoadStateUtil.getInstance().setContextAndInitView(this));
        mAdapter = mVideoAdapter;
        LinearLayoutManager videoLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManger = videoLayoutManager;
    }

    private void initIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            mType = intent.getStringExtra("type");
        }
    }


    @Override
    public void onRefresh() {
        if (mAdapter.isLoading()) {//正在加载更多，则不刷新
            mSrLayoutUsrVideoClassificationActivity.setRefreshing(false);
            AllUtils.showToast(this, "正在加载更多");
            return;
        }
        if (!isLoading) {
            isLoading = true;
            mCurrPage = 1;
            initData(false);
        }
    }

    @Override
    public void onLoadMoreRequested() {
        if (mSrLayoutUsrVideoClassificationActivity.isRefreshing()) {//正在刷新，则不加载更多
            mAdapter.loadMoreComplete();
            return;
        }
        initData(true);
    }

    @OnClick({R.id.iv_return_title_bar, R.id.tv_delete_title_bar, R.id.tv_choose_title_bar, R.id.tv_choose_all_title_bar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_return_title_bar:
                finish();
                break;
            case R.id.tv_delete_title_bar:
                break;
            case R.id.tv_choose_title_bar:
                if ("选择".equals(mTvChooseTitleBar.getText())) {
                    mTvChooseTitleBar.setText("取消");
                    mTvDeleteTitleBar.setVisibility(View.VISIBLE);
                    mTvChooseAllTitleBar.setVisibility(View.VISIBLE);
                    chooseClick(true);
                }else {
                    mTvChooseTitleBar.setText("选择");
                    mTvDeleteTitleBar.setVisibility(View.INVISIBLE);
                    mTvChooseAllTitleBar.setVisibility(View.INVISIBLE);
                    chooseClick(false);
                }
                break;
            case R.id.tv_choose_all_title_bar:
                if ("全选".equals(mTvChooseAllTitleBar.getText())) {
                    mTvChooseAllTitleBar.setText("全不选");
                    chooseAll(true);
                }else {
                    mTvChooseAllTitleBar.setText("全选");
                    chooseAll(false);
                }
                break;
        }
    }

    /**是否将条目进行全选
     * @param chooseAll
     */
    private void chooseAll(boolean chooseAll) {
        switch (mType) {
            case "0"://片单
                formShowDelete(chooseAll);
                break;
            case "1"://影视
                videoShowDelete(chooseAll);
                break;
            case "2"://历史记录
                hitoryShowDelete(chooseAll);
                break;
        }
    }

    /**判断选择是否被点击，
     * @param isChooseClick true显示条目的删除 false不显示
     */
    public void chooseClick(boolean isChooseClick){

                switch (mType) {
            case "0"://片单
                mFormAdapter = (RecyclerItemFormUserVideoClassificationAdapter) mAdapter;
                mFormAdapter.setShowDelete(isChooseClick);
                break;
            case "1"://影视
                mVideoAdapter = (RecyclerItemVideoUserVideoClassificationAdapter) mAdapter;
                mVideoAdapter.setShowDelete(isChooseClick);
                break;
            case "2"://历史记录
                mHistoryAdapter = (RecyclerItemHistoryUserVideoClassificationAdapter) mAdapter;
                mHistoryAdapter.setShowDelete(isChooseClick);
                break;
        }


    }

    private void hitoryShowDelete(boolean chooseAll) {
        if (mHistoryList != null && mHistoryList.size()>0) {
            for (UserVideoPlayHistoryBean userVideoPlayHistoryBean : mHistoryList) {
                userVideoPlayHistoryBean.isSelcted = chooseAll;
            }
            mHistoryAdapter.notifyDataSetChanged();
        }

    }

    private void videoShowDelete(boolean chooseAll) {
        if (mVideoList != null && mHistoryList.size()>0) {
            for (UserVideoClassificationVideoBean.DataBean.VideoBean videoBean : mVideoList) {
                videoBean.isSelcted = chooseAll;
            }
            mVideoAdapter.notifyDataSetChanged();
        }
    }

    private void formShowDelete(boolean chooseAll) {
        if (mFormList != null && mFormList.size()>0) {
            for (UserVideoCassificationFormBean.DataBean.VideoListBean videoListBean : mFormList) {
                videoListBean.isSelcted = chooseAll;
            }
            mFormAdapter.notifyDataSetChanged();
        }
    }
}