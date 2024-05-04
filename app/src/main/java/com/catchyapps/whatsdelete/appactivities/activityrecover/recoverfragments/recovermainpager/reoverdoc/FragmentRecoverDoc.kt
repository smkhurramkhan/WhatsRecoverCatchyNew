package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas.H_DOCUMENT_TYPE
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityrecover.TypesIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.appactivities.activityrecover.SharedVM
import com.catchyapps.whatsdelete.appactivities.activityrecover.ViewStateShared
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc.RecoverDocsAdapter.DocsAdapterCallbacks
import com.catchyapps.whatsdelete.databinding.DocFragmentLayoutBinding
import timber.log.Timber
import java.io.*
import java.util.*


class FragmentRecoverDoc : Fragment(), ActionMode.Callback, DocsAdapterCallbacks {
    private var hRecoverDocsAdapter: RecoverDocsAdapter? = null
    private var objectList: MutableList<EntityFiles> = ArrayList()
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    private lateinit var hFragmentDocBinding: DocFragmentLayoutBinding
    private var hFragmentDocViewModel: VMFragmentDoc? = null
    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var layoutManager: LinearLayoutManager? = null
    private var loading = true
    private var pageNumber = 1
    private var hPrefs: MyAppSharedPrefs? = null
    private val hSharedVM by activityViewModels<SharedVM>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setHasOptionsMenu(true)
        hFragmentDocBinding = DocFragmentLayoutBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentDocBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        hFragmentDocViewModel = ViewModelProvider(this)[VMFragmentDoc::class.java]
        hPrefs = MyAppSharedPrefs(
            requireContext()
        )
        hSetupRecyclerView()
        hSubscribeObservers()
        hSetupLoadMoreListener()

    }

    private fun hSetupLoadMoreListener() {
        hFragmentDocBinding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
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
                        hFragmentDocViewModel?.hLoadMoreItems(pageNumber)
                    }
                }
            }
        )
    }

    private fun hSubscribeObservers() {
        hFragmentDocViewModel?.hDocsListLD?.observe(
            viewLifecycleOwner
        ) { docsList ->
            docsList?.let { hSetViewsData(it) }
        }

        hSharedVM.hSharedViewStateLD.observe(viewLifecycleOwner) {
            when (it) {
                is ViewStateShared.OnUpdateDocs ->
                    hFragmentDocViewModel!!.hLoadMoreItems(pageNumber)

                else -> Unit
            }
        }
    }

    private fun hSetViewsData(docsList: List<EntityFiles>) {
        loading = false
        if (docsList.isNotEmpty()) {
            hFragmentDocBinding.recyclerView.visibility = View.VISIBLE
            hFragmentDocBinding.layoutNotfound.visibility = View.GONE
            hRecoverDocsAdapter!!.hAddItems(docsList.toMutableList())
            objectList = hRecoverDocsAdapter!!.hGetLists() as ArrayList<EntityFiles>
        } else {
            hFragmentDocBinding.recyclerView.visibility = View.GONE
            hFragmentDocBinding.layoutNotfound.visibility = View.VISIBLE
        }
    }

    private fun hHandleLongClick(position: Int) {
        if (!isMultiSelect) {
            selectedIds = SparseArray()
            isMultiSelect = true
            if (actionMode == null) {
                try {
                    actionMode = (Objects.requireNonNull(requireActivity()) as MainRecoverActivity)
                        .startSupportActionMode(this@FragmentRecoverDoc)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        multiSelect(position)
    }

    private fun hHandleSingleClick(view: View, position: Int) {
        if (isMultiSelect) {
            multiSelect(position)
        } else {
            if (position < objectList.size) {
                try {

                    objectList[position].fileUri?.let {
                        objectList[position].title?.let { it1 ->
                            openFile(
                                it,
                                it1
                            )
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun openFile(fileUri: String, url: String) {
        requireContext()
        Timber.d("Path : $url")

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fileUri.toUri()
        } else {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                File(url)
            )
        }


        val intent = Intent(Intent.ACTION_VIEW)
        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword")
        } else if (url.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf")
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel")
        } else if (url.contains(".zip") || url.contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/x-wav")
        } else if (url.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf")
        } else if (url.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain")
        } else {
            intent.setDataAndType(uri, "*/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            MyAppUtils.showToast(requireContext(), getString(R.string.you_cannot_open_this_file))
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (MyAppPermissionUtils.hasPermissionPost10(requireContext())) {

                true

            } else {

                Timber.d("Permission not Granted")

                if (hPrefs?.getPermissionDialogFirstTime() == false) {
                    MyAppPermissionUtils.hShowUriPermissionDialog(
                        context = requireContext(),
                        description = CleanerConstans.hGetDialogText(
                            H_DOCUMENT_TYPE,
                            requireContext()
                        ),
                    ) {
                        hSharedVM.hLaunchIntent(TypesIntent.H_URI)
                    }
                    hPrefs?.setPermissionDialogFirstTime(true)
                } else {
                    hFragmentDocBinding.recyclerView.visibility = View.GONE
                    hFragmentDocBinding.permissionDialoge.visibility = View.VISIBLE
                    hFragmentDocBinding.btnPositive.setOnClickListener {
                        hSharedVM.hLaunchIntent(TypesIntent.H_URI)
                    }
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
                    val alertBuilder = AlertDialog.Builder(requireActivity())
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle(getString(R.string.permission_necessary))
                    alertBuilder.setMessage(getString(R.string.storage_permission_is_necessary_to_status_media_files))
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

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            hFragmentDocBinding.permissionDialoge.visibility = View.GONE
            hFragmentDocBinding.recyclerView.visibility = View.VISIBLE
            hFragmentDocViewModel?.hLoadMoreItems(pageNumber)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hFragmentDocViewModel?.hLoadMoreItems(pageNumber)
            } else {
                checkAgain()
            }
        }
    }

    private fun checkAgain() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                Objects.requireNonNull(requireActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val alertBuilder = AlertDialog.Builder(requireActivity())
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle(getString(R.string.permission_necessary))
            alertBuilder.setMessage(getString(R.string.storage_permission_is_necessary_to_status_media_files))
            alertBuilder.setPositiveButton(
                R.string.yes
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
    }

    private fun hSetupRecyclerView() {
        hRecoverDocsAdapter = RecoverDocsAdapter(requireActivity(), objectList)
        hFragmentDocBinding.recyclerView.setHasFixedSize(true)
        hRecoverDocsAdapter!!.hSetItemCallbacks(this)
        layoutManager = GridLayoutManager(requireContext(), 3)
        hFragmentDocBinding.recyclerView.layoutManager = layoutManager
        hFragmentDocBinding.recyclerView.adapter = hRecoverDocsAdapter
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = hRecoverDocsAdapter!!.getItem(position)
            if (actionMode != null) {
                if (selectedIds.indexOfKey(position) > -1) selectedIds.remove(position) else selectedIds.put(
                    position,
                    data.title
                )
                if (selectedIds.size() > 0) actionMode!!.title =
                    selectedIds.size()
                        .toString() + getString(R.string.items_selected) //show selected item count on action mode.
                else {
                    actionMode?.title = "" //remove item count from action mode.
                    actionMode?.finish() //hide action mode.
                }
                hRecoverDocsAdapter?.setSelectedIds(selectedIds)
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
        hRecoverDocsAdapter!!.setSelectedIds(selectedIds)
    }

    private fun alertDeleteConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireActivity())
        if (selectedIds.size() > 1)
            builder.setMessage(getString(R.string.delete)  + selectedIds.size() + getString(R.string.document) + "?")
        else builder.setMessage(
            getString(R.string.delete_selected_documents)
        )
        builder.setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface, which: Int ->
            try {
                for (i in 0 until selectedIds.size()) {
                    val fileEntity = objectList[selectedIds.keyAt(i)]
                    val fDelete = File(fileEntity.filePath!!)
                    if (fDelete.exists()) {
                        val del = fDelete.delete()
                    }
                    objectList.removeAt(selectedIds.keyAt(i))
                }
                if (objectList.size > 0) {
                    hRecoverDocsAdapter?.notifyDataSetChanged()
                } else {
                    hFragmentDocBinding.recyclerView.visibility = View.GONE
                    hFragmentDocBinding.layoutNotfound.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.cancel()
            actionMode?.title = "" //remove item count from action mode.
            actionMode?.finish() //
        }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun shareMultipleFiles() {
        try {
            val selectedUri = ArrayList<Uri>()
            for (i in 0 until selectedIds.size()) {
                val fileEntity = objectList[selectedIds.keyAt(i)]
                val uri = fileEntity.filePath
                val u = FileProvider.getUriForFile(
                    Objects.requireNonNull(requireContext()),
                    Objects.requireNonNull(requireActivity()).packageName + ".provider",
                    File(uri.toString())
                )
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
        intent.type = "application/octet-stream"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    fun hExecuteSearch(newText: String?) {
        hRecoverDocsAdapter?.filter?.filter(newText)
    }

    override fun hSingleClick(v: View?, position: Int, files: EntityFiles?) {
        v?.let { hHandleSingleClick(it, position) }
    }

    override fun hLongClick(v: View?, position: Int, files: EntityFiles?) {
        hHandleLongClick(position)
    }

    override fun hMoreClick(v: View?, position: Int, files: EntityFiles?) {
        if (actionMode == null) {
            val popup = v?.let { PopupMenu(requireActivity(), it) }
            //Inflating the Popup using xml file
            popup?.menuInflater?.inflate(R.menu.popup_audio_menu, popup.menu)

            popup?.menu?.getItem(0)?.setOnMenuItemClickListener {
                val name = files?.filePath!!.substring(files.filePath?.lastIndexOf("/")!! + 1)
                files.setIsfav(true)
                checkDocFolder()

                Timber.d("files path is ${files.filePath}")

                val cw = ContextWrapper(context)
                val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                val dir = File(directory, MyAppConstants.WA_FAV_DOC)

                copyFile(files.filePath, dir.absolutePath + "/" + name)
                Toast.makeText(requireContext(),
                    getString(R.string.added_to_favourites), Toast.LENGTH_LONG).show()
                true
            }

            if (files?.isIsfav == true) {
                popup?.menu?.findItem(R.id.item_favorite)?.isVisible = false
                popup?.menu?.findItem(R.id.item_unfavorite)?.isVisible = true
            }


            popup?.menu?.getItem(1)?.setOnMenuItemClickListener {

                val cw = ContextWrapper(context)
                val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                val dir = File(directory, MyAppConstants.WA_FAV_DOC)

                val file = File(dir.absolutePath + "/" + files?.title)


                val deleted: Boolean = file.delete()
                if (deleted) {
                    files?.setIsfav(false)
                    Toast.makeText(requireContext(),
                        getString(R.string.removed_from_favourites), Toast.LENGTH_LONG)
                        .show()
                }
                true


            }
            popup?.menu?.getItem(2)?.setOnMenuItemClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(files?.filePath))
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                sharingIntent.type = "application/octet-stream"
                sharingIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + requireActivity().packageName
                )
                startActivity(Intent.createChooser(sharingIntent, "Share via"))
                true
            }
            popup?.menu?.getItem(3)?.setOnMenuItemClickListener {
                files?.let { infoDialog(it) }
                true
            }
            popup?.menu?.getItem(4)?.setOnMenuItemClickListener {
                files?.let { deleteDialog(position, it) }
                true
            }
            popup?.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun infoDialog(files: EntityFiles) {
        try {
            val builder = AlertDialog.Builder(requireContext())
            val inflater = requireActivity().layoutInflater
            val dialogView: View = inflater.inflate(R.layout.dialog_info_layout, null)
            builder.setView(dialogView)
            val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
            val tvFullName = dialogView.findViewById<TextView>(R.id.tvFullName)
            val tvPath = dialogView.findViewById<TextView>(R.id.tvPath)
            val tvModificationDate = dialogView.findViewById<TextView>(R.id.tvModificationDate)
            val tvSize = dialogView.findViewById<TextView>(R.id.tvSize)
            tvFileName.text = files.title
            tvFullName.text = files.title
            tvPath.text = files.filePath
            tvModificationDate.text =
                files.timeStamp?.toLong()?.let { MyAppUtils.getFileDateTime(it) }
            val file = File(files.filePath!!)
            var fileSize = (file.length() / 1024).toString().toInt()
            if (fileSize < 1024) {
                tvSize.text = "$fileSize Kb"
            } else {
                fileSize /= 1024
                tvSize.text = "$fileSize Mb"
            }
            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.show()
        } catch (ignored: Exception) {
        }
    }

    private fun deleteDialog(position: Int, files: EntityFiles) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_delete))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.delete)) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                try {
                    val delete = DocumentFile.fromSingleUri(
                        requireContext(),
                        Uri.parse(files.fileUri)
                    )?.delete()
                    Timber.d("Deleted $delete")
                    if (delete == true) {
                        hRecoverDocsAdapter!!.removeItem(position)
                    }
                } catch (e: Exception) {
                    Timber.d("Exception ${e.message}")
                }
            } else {
                delete(position, files)
            }

        }
        builder.setNegativeButton(
            getString(R.string.cancel)
        ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun delete(position: Int, files: EntityFiles) {
        val path = files.filePath
        val file = File(path!!)
        try {
            if (file.exists()) {
                val del = file.delete()
                if (del) {
                    try {
                        MediaScannerConnection.scanFile(
                            requireContext(), arrayOf(path, path), arrayOf("application/pdf"),
                            object : MediaScannerConnection.MediaScannerConnectionClient {
                                override fun onMediaScannerConnected() {}
                                override fun onScanCompleted(path: String, uri: Uri) {}
                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    MyAppUtils.showToast(requireContext(), getString(R.string.something_went_wrong))
                }
            }
            hRecoverDocsAdapter!!.removeItem(position)
        } catch (e: Exception) {
            Timber.d("Exception : $e")
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        val mSearch = menu.findItem(R.id.actionSearch)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = getString(R.string.search)
        mSearchView.isSubmitButtonEnabled = false
        @SuppressLint("CutPasteId") val txtSearch =
            mSearchView.findViewById<EditText>(R.id.search_src_text)
        txtSearch.setTextColor(Color.WHITE)
        @SuppressLint("CutPasteId") val searchTextView =
            mSearchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        try {
            val mCursorDrawableRes =
                TextView::class.java.getDeclaredField(getString(R.string.mCursorDrawableRes))
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
                return false
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
                hSharedVM.hLaunchIntent(TypesIntent.H_SETTINGS)
                return false
            }
            else -> {
            }
        }
        return false
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123
        var mActionMode: ActionMode? = null
    }


    private fun copyFile(inputPath: String?, outputPath: String?) {
        val `in`: InputStream
        val out: OutputStream

        try {
            `in` = FileInputStream(inputPath)
            out = FileOutputStream(outputPath)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()

            // write the output file (You have now copied the file)
            out.flush()
            out.close()
        } catch (e: java.lang.Exception) {
            Timber.d("Exception is ${e.message}")
            e.printStackTrace()
        }
    }


    private fun checkDocFolder() {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
        val dir = File(directory, MyAppConstants.WA_FAV_DOC)
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
            Timber.d("create directory path ${dir.absolutePath}")

        }
        if (isDirectoryCreated) {
            Timber.d("Already Created")
        }
    }
}
