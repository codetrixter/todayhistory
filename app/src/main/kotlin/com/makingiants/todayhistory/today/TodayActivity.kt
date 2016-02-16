package com.makingiants.todayhistory.today

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.makingiants.today.api.repository.history.HistoryRepository
import com.makingiants.today.api.repository.history.pojo.Event
import com.makingiants.todayhistory.BuildConfig
import com.makingiants.todayhistory.R
import com.makingiants.todayhistory.base.BaseActivityView
import com.makingiants.todayhistory.utils.DateManager
import com.makingiants.todayhistory.utils.NetworkChecker
import com.makingiants.todayhistory.utils.SpacesItemDecoration
import com.makingiants.todayhistory.utils.refresh_layout.ScrollEnabler
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.today_activity.*

class TodayActivity : BaseActivityView(), TodayView, SwipeRefreshLayout.OnRefreshListener, ScrollEnabler {
    val PARCEL_PRESENTER = "presenter"
    var mPresenter: TodayPresenter? = null
    private var mAdapter: TodayAdapter? = null

    //<editor-fold desc="Activity">

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.today_activity)
        activateToolbar(R.string.title_activity_today)

        mPresenter = savedInstanceState?.getParcelable(PARCEL_PRESENTER) ?: TodayPresenter(DateManager())
        mPresenter?.onCreate(this, HistoryRepository(), NetworkChecker(applicationContext))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(PARCEL_PRESENTER, mPresenter)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy()

        swipeRefreshLayout?.apply {
            setScrollEnabler(null)
            setOnRefreshListener(null)
        }
    }
    //</editor-fold>

    //<editor-fold desc="TodayView">
    override fun initViews() {
        mAdapter = TodayAdapter(Picasso.with(applicationContext))

        recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(applicationContext)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(SpacesItemDecoration(16))
        }

        swipeRefreshLayout.apply {
            setOnRefreshListener(this@TodayActivity)
            setScrollEnabler(this@TodayActivity)
            setColorSchemeColors(R.color.colorAccent, R.color.colorPrimary)
        }

        val adRequest: AdRequest
        if (BuildConfig.DEBUG) {
            adRequest = AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("027c6ee5571a8376")
                    .build();
        } else {
            adRequest = AdRequest.Builder().build();
        }

        adsView.loadAd(adRequest);
    }

    override fun showEvents(events: List<Event>) {
        recyclerView?.setVisibility(View.VISIBLE)
        mAdapter?.setEvents(events)
    }

    override fun hideEvents() {
        recyclerView?.setVisibility(View.GONE)
    }

    override fun showEmptyViewProgress() {
        progressView?.visibility = View.VISIBLE
    }

    override fun dismissEmptyViewProgress() {
        progressView?.visibility = View.GONE
    }

    override fun showReloadProgress() {
        swipeRefreshLayout?.setRefreshing(true)
    }

    override fun dismissReloadProgress() {
        swipeRefreshLayout?.setRefreshing(false)
    }

    override fun showErrorView(title: String, message: String) {
        errorTitleView?.text = title
        errorMessageTextView?.text = message
        errorView?.visibility = View.VISIBLE
    }

    override fun hideErrorView() {
        errorView?.visibility = View.GONE
    }

    override fun showErrorToast(message: String) {
        showToast(message)
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        emptyView?.visibility = View.GONE
    }

    override fun showErrorDialog(throwable: Throwable) {
        super.showError(throwable)
    }
    //</editor-fold>

    //<editor-fold desc="SwipeRefreshLayout.OnRefreshListener">
    override fun onRefresh() {
        mPresenter?.updateItems()
    }
    //</editor-fold>

    //<editor-fold desc="ScrollEnabler">
    override fun canScrollUp(): Boolean {
        return recyclerView?.getVisibility() === View.VISIBLE &&
                recyclerView?.canScrollVertically(-1) ?: false
    }
    //</editor-fold>
}