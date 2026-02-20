package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activitypreview.PreviewScreen
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.appactivities.activityrecover.SharedVM
import com.catchyapps.whatsdelete.appactivities.activityrecover.TypesIntent
import com.catchyapps.whatsdelete.appactivities.activitysetting.SettingsScreen
import com.catchyapps.whatsdelete.appclasseshelpers.RVTouchListener
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.basicapputils.hide
import com.catchyapps.whatsdelete.basicapputils.show
import com.catchyapps.whatsdelete.databinding.ImagesFragmentLayoutBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import timber.log.Timber
import java.io.File
import java.util.Objects

class RecoverMediaFragment : Fragment(), ActionMode.Callback {
    private var hAdapterMedia: RecoverMediaAdapter? = null
    private var hAdapterFiles: MutableList<EntityFiles?>? = null
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    private var layoutManager: GridLayoutManager? = null
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var loading = false
    private var pageNumber = 1
    private var hFragmentImagesBinding: ImagesFragmentLayoutBinding? = null
    private var hFragmentMediaViewModel: FragmentRecoverMediaVM? = null
    private val isImage = false
    private val hSharedVM by activityViewModels<SharedVM>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        hInitVariables()
    }

    private fun hInitVariables() {
        hAdapterFiles = ArrayList()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Onview created.")
        hFragmentMediaViewModel = ViewModelProvider(this).get(FragmentRecoverMediaVM::class.java)
        hSetUpRecyclerView()
        hSetUpLoadMoreListener()
        hSetupItemTouchListener()
        hSubscribeObservers()


        if (checkPermission()) {
            hFragmentImagesBinding?.hProgressbar?.show()
            hFragmentMediaViewModel?.hSetFragmentType(arguments)
        }
    }

    private fun hSubscribeObservers() {
        hFragmentMediaViewModel?.hVideoFileListLd?.observe(viewLifecycleOwner) { videoList: List<EntityFiles>? ->
            hFragmentImagesBinding?.hProgressbar?.hide()
            hFragmentImagesBinding?.hMainCard?.show()
            if (videoList == null || videoList.isEmpty()) {
                Timber.d("Video list null or empty")
                hFragmentImagesBinding?.recyclerView?.hide()
                hFragmentImagesBinding?.tvNoImageVideo?.text = getString(R.string.no_video_detected_yet)
                hFragmentImagesBinding?.tvNoImageVideo?.show()
                hFragmentImagesBinding?.tvNoImageVideo
                    ?.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        R.drawable.nofiles_img,
                        0,
                        0
                    )
            } else {
                Timber.d("Not null")
                hSetViewsData(videoList)
            }
        }
        hFragmentMediaViewModel?.hImagesListLd?.observe(viewLifecycleOwner) { imageList: List<EntityFiles>? ->
            hFragmentImagesBinding?.hProgressbar?.hide()
            hFragmentImagesBinding?.hMainCard?.show()
            if (imageList == null || imageList.isEmpty()) {
                Timber.d("Image list null or empty")
                hFragmentImagesBinding?.recyclerView?.hide()
                hFragmentImagesBinding?.tvNoImageVideo?.show()
                hFragmentImagesBinding?.tvNoImageVideo
                    ?.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        R.drawable.nofiles_img,
                        0,
                        0
                    )
            } else {
                Timber.d("Not null")
                hSetViewsData(imageList)
            }
        }
    }

    private fun hSetViewsData(list: List<EntityFiles>) {

        loading = false
        if (list.isNotEmpty()) {
            hFragmentImagesBinding?.recyclerView?.show()
            hFragmentImagesBinding?.tvNoImageVideo?.hide()
            hAdapterMedia?.hAddItems(list)
            val fileEntities = hAdapterMedia?.hGetLists()
            Timber.d("List size  %s", fileEntities?.size?:0)
            hAdapterFiles = fileEntities as ArrayList<EntityFiles?>
        } else {

            hFragmentImagesBinding?.recyclerView?.hide()
            hFragmentImagesBinding?.tvNoImageVideo?.show()
            hFragmentImagesBinding?.tvNoImageVideo
                ?.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.nofiles_img,
                    0,
                    0
                )
        }
    }

    private fun hSetupItemTouchListener() {
        hFragmentImagesBinding?.recyclerView?.addOnItemTouchListener(
            RVTouchListener(
                requireActivity(),
                hFragmentImagesBinding?.recyclerView,
                object : RVTouchListener.ClickListener {
                    override fun shouldHandleClick(view: View, position: Int, e: MotionEvent): Boolean {
                        // Don't handle when user tapped share or delete — let those icon clicks work only.
                        val ivShare = view.findViewById<View>(R.id.ivShare)
                        val ivDelete = view.findViewById<View>(R.id.ivDelete)
                        if (ivShare != null && isTouchInsideView(view, ivShare, e)) return false
                        if (ivDelete != null && isTouchInsideView(view, ivDelete, e)) return false
                        return true
                    }

                    override fun onClick(view: View, position: Int, e: MotionEvent) {
                        if (position < 0 || position >= (hAdapterMedia?.itemCount ?: 0)) return
                        if (hAdapterMedia?.getItem(position) == null) return
                        if (isMultiSelect) {
                            multiSelect(position)
                        } else {
                            // Open preview only when tap is on the image, not on name or elsewhere
                            val mainImage = view.findViewById<View>(R.id.mainImageView)
                            if (mainImage == null || !isTouchInsideView(view, mainImage, e)) return
                            try {
                                val filePath = hAdapterMedia?.getItem(position)?.filePath
                                if (filePath != null) {
                                    val intent = Intent(requireContext(), PreviewScreen::class.java)
                                    intent.putExtra("file_path", filePath)
                                    startActivity(intent)
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }

                    override fun onLongClick(view: View, position: Int, e: MotionEvent) {
                        if (position < 0 || position >= (hAdapterMedia?.itemCount ?: 0)) return
                        if (hAdapterMedia?.getItem(position) == null) return
                        // Only start multi-select when long-press is on the image
                        val mainImage = view.findViewById<View>(R.id.mainImageView)
                        if (mainImage != null && !isTouchInsideView(view, mainImage, e)) return
                        if (!isMultiSelect) {
                            selectedIds = SparseArray()
                            isMultiSelect = true
                            if (actionMode == null) {
                                try {
                                    actionMode = (requireActivity() as MainRecoverActivity)
                                        .startSupportActionMode(this@RecoverMediaFragment)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        multiSelect(position)
                    }
                }
            )
        )
    }

    private fun isTouchInsideView(rowView: View, childView: View, e: MotionEvent): Boolean {
        val rowLoc = IntArray(2)
        rowView.getLocationInWindow(rowLoc)
        val childLoc = IntArray(2)
        childView.getLocationInWindow(childLoc)
        val x = e.rawX - rowLoc[0]
        val y = e.rawY - rowLoc[1]
        val left = childLoc[0] - rowLoc[0]
        val top = childLoc[1] - rowLoc[1]
        return x >= left && x <= left + childView.width &&
                y >= top && y <= top + childView.height
    }

    private fun hSetUpLoadMoreListener() {
        hFragmentImagesBinding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int, dy: Int,
            ) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager?.itemCount!!
                lastVisibleItem = layoutManager!!
                    .findLastCompletelyVisibleItemPosition()
                if (!loading && lastVisibleItem == totalItemCount - 1) {
                    pageNumber++
                    loading = true
                    hFragmentMediaViewModel?.hLoadMoreItems(pageNumber)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        hFragmentImagesBinding = ImagesFragmentLayoutBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentImagesBinding?.root
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = hAdapterMedia!!.getItem(position)
            if (actionMode != null) {
                if (selectedIds.indexOfKey(position) > -1)
                    selectedIds.remove(position) else selectedIds.put(position, data.title)
                if (selectedIds.size() > 0) actionMode?.title =
                    selectedIds.size().toString() + "  Selected" //show selected item count on action mode.
                else {
                    actionMode?.title = "" //remove item count from action mode.
                    actionMode?.finish() //hide action mode.
                }
                hAdapterMedia?.setSelectedIds(selectedIds)
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mActionMode = mode
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.file_menu_select_fragment, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            alertDeleteConfirmation()
            return true
        } else if (item.itemId == R.id.action_share) {
            shareMultipleFiles()
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        mActionMode = null
        isMultiSelect = false
        selectedIds = SparseArray()
        hAdapterMedia!!.setSelectedIds(selectedIds)
    }


    private fun alertDeleteConfirmation() {
        val builder = AlertDialog.Builder(requireActivity())
        val imageVideo = if (isImage) "Image" else "Video"
        if (selectedIds.size() > 1) builder.setMessage("Delete " + selectedIds.size() + " selected " + imageVideo + "?") else builder.setMessage("Delete selected $imageVideo?")
        builder.setPositiveButton("DELETE") { dialog: DialogInterface, which: Int ->
            try {
                for (i in 0 until selectedIds.size()) {
                    val fileEntity = hAdapterMedia!!.getItem(selectedIds.keyAt(i))
                    val fDelete = File(fileEntity.filePath ?: "")
                    if (fDelete.exists()) {
                        val del = fDelete.delete()
                        Timber.d("del-res is$del")
                    }
                    hAdapterFiles?.remove(fileEntity)
                }
                val list = hAdapterFiles?.filterNotNull()?.toList() ?: emptyList()
                hAdapterMedia?.hAddItems(list)
                if (list.isNotEmpty()) {
                    hFragmentImagesBinding?.recyclerView?.show()
                    hFragmentImagesBinding?.tvNoImageVideo?.hide()
                } else {
                    hFragmentImagesBinding?.recyclerView?.hide()
                    hFragmentImagesBinding?.tvNoImageVideo?.show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.cancel()
            actionMode?.title = ""
            actionMode?.finish() //
        }.setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkPermission()) hFragmentMediaViewModel!!.hSetFragmentType(arguments)
            } else {
                checkAgain()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            hFragmentImagesBinding?.permissionDialoge?.hide()
            if (hAdapterFiles?.isNotEmpty() == true) {
                hFragmentImagesBinding?.hProgressbar?.hide()
                hFragmentImagesBinding?.recyclerView?.show()
                hFragmentImagesBinding?.tvNoImageVideo?.hide()
            } else {
                // Data not loaded yet, keep showing loader or wait for observer
                hFragmentImagesBinding?.tvNoImageVideo?.hide()
            }
        }
    }


    private fun checkAgain() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val alertBuilder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle(getString(R.string.permission_necessary))
            alertBuilder.setMessage(getString(R.string.storage_permission_is_necessary_to_status_media_files))
            alertBuilder.setPositiveButton(
                android.R.string.yes
            ) { dialog: DialogInterface?, which: Int ->
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                )
            }
            val alert = alertBuilder.create()
            alert.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        }
    }

    private fun hSetUpRecyclerView() {
        hFragmentImagesBinding?.recyclerView?.setHasFixedSize(true)
        layoutManager = GridLayoutManager(requireActivity(), 2)
        hFragmentImagesBinding?.recyclerView?.layoutManager = layoutManager
        hAdapterMedia = RecoverMediaAdapter(requireActivity(), hAdapterFiles?.toList() as List<EntityFiles>, requireActivity())
        hFragmentImagesBinding?.recyclerView?.adapter = hAdapterMedia
    }

    private fun shareMultipleFiles() {
        try {
            val selectedUri = ArrayList<Uri>()
            for (i in 0 until selectedIds.size()) {
                val fileEntity = hAdapterFiles!![selectedIds.keyAt(i)]
                val uri = fileEntity!!.filePath
                val u = FileProvider.getUriForFile(requireContext(), requireActivity().packageName + ".provider", File(uri))
                selectedUri.add(u)
            }
            actionMode?.title = ""
            actionMode?.finish()
            shareMultiple(selectedUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultiple(files: ArrayList<Uri>?) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    fun hExecuteSearch(newText: String?) {
        hAdapterMedia?.filter?.filter(newText)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        val mSearch = menu.findItem(R.id.actionSearch)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = getString(R.string.search)
        mSearchView.isSubmitButtonEnabled = false
        @SuppressLint("CutPasteId") val txtSearch = mSearchView.findViewById<EditText>(R.id.search_src_text)
        txtSearch.setTextColor(Color.WHITE)
        @SuppressLint("CutPasteId") val searchTextView = mSearchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        try {
            val mCursorDrawableRes = TextView::class.java.getDeclaredField(getString(R.string.drawablessd))
            mCursorDrawableRes.isAccessible = true
            mCursorDrawableRes[searchTextView] = R.drawable.icon_cursor_png
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                hExecuteSearch(newText)
                return true
            }
        })
        val viewGift = menu.findItem(R.id.action_prem)
        val view = viewGift.actionView
        view?.setOnClickListener {
            showPremiumDialog()
        }
        super.onCreateOptionsMenu(menu, inflater)

    }

    private fun showPremiumDialog() {
        val intent = Intent(requireContext(), ActivityPremium::class.java)
        startActivity(intent)
    }


    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(requireContext(), SettingsScreen::class.java))
                return false
            }
            else -> {
            }
        }
        return false
    }


    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (MyAppPermissionUtils.hasPermissionPost10(requireContext())) {
                true
            } else {
                Timber.d("Permission not Granted")
                hFragmentImagesBinding?.hProgressbar?.hide()
                hFragmentImagesBinding?.recyclerView?.hide()
                hFragmentImagesBinding?.tvNoImageVideo?.hide()
                hFragmentImagesBinding?.permissionDialoge?.show()
                hFragmentImagesBinding?.btnPositive?.setOnClickListener {
                    Timber.d("button clicked")
                    hSharedVM.hLaunchIntent(TypesIntent.H_URI)
                }
                false
            }

        } else if (currentAPIVersion >= Build.VERSION_CODES.M && currentAPIVersion <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    Objects.requireNonNull(requireContext()),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        Objects.requireNonNull(requireActivity()),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("Storage permission is necessary to Download Images and Videos!!!")
                    alertBuilder.setPositiveButton(
                        R.string.ok
                    ) { _: DialogInterface?, _: Int ->
                        ActivityCompat.requestPermissions(
                            Objects.requireNonNull(requireActivity()),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                    )
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123
        var mActionMode: ActionMode? = null
    }
}