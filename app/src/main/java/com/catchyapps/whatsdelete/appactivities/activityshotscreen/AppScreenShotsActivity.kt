package com.catchyapps.whatsdelete.appactivities.activityshotscreen

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import kotlinx.coroutines.launch

class AppScreenShotsActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var rvScreenShots: RecyclerView? = null
    private var screenShotsEntityList: MutableList<Any>? = null
    private var toolbar: Toolbar? = null

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_shots_screen)
        init()
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.title = getString(R.string.screenshots)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        lifecycleScope.launch {
            AppHelperDb.getAllScreenShots()?.toMutableList()
                ?.let { screenShotsEntityList?.addAll(it) }
        }
        setUpRecyclerView()

    }

    private fun init() {
        rvScreenShots = findViewById(R.id.rv_screenshots)
        toolbar = findViewById(R.id.toolbar)
        screenShotsEntityList = ArrayList()
    }

    private fun setUpRecyclerView() {
        rvScreenShots?.layoutManager = GridLayoutManager(this, 1)
        val recyclerViewAdapter =
            ShotsAdapter(screenShotsEntityList!!, this, this@AppScreenShotsActivity)
        rvScreenShots?.adapter = recyclerViewAdapter
    }
}