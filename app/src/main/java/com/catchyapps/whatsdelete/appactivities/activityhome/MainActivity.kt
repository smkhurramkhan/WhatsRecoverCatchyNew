package com.catchyapps.whatsdelete.appactivities.activityhome

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitycollection.ActivityStatusSavedCollections
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.DirectChatScreenActivity
import com.catchyapps.whatsdelete.appactivities.activityhome.homeadapter.HomeMenuAdapter
import com.catchyapps.whatsdelete.appactivities.activityhome.homeadapter.StatuseMenuAdapter
import com.catchyapps.whatsdelete.appactivities.activityhome.homeadapter.ToolsMenuAdapter
import com.catchyapps.whatsdelete.appactivities.activityhome.menumodels.ModelMenu
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityprivacypolicy.PrivacyPolicyScreen
import com.catchyapps.whatsdelete.appactivities.activityquatations.ActivityQuotationsScreen
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.ActivityStatusMain
import com.catchyapps.whatsdelete.appactivities.activitystickers.ActivityStickersScreen
import com.catchyapps.whatsdelete.appactivities.activitystylishtext.StylishTextActivity
import com.catchyapps.whatsdelete.appactivities.activitytextrepeater.TextRepeaterScreen
import com.catchyapps.whatsdelete.appactivities.activitytexttoemoji.TextToEmojiScreen
import com.catchyapps.whatsdelete.appactivities.activitywhatsappweb.WhatsWebScreen
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.WACleanerScreen
import com.catchyapps.whatsdelete.appactivities.myapplanguage.ChangeLanguageActivity
import com.catchyapps.whatsdelete.appadsmanager.GoogleMobileAdsConsentManager
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppDataUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppExcelUtils
import com.catchyapps.whatsdelete.databinding.ScreenHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import timber.log.Timber
import java.io.File
import java.util.Locale

class MainActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity(),
    com.catchyapps.whatsdelete.appactivities.activitydirectchat.namesetlistener.SetMyName {
    private var appSharedPreferences: MyAppSharedPrefs? = null
    private var number: String? = null
    var message: String? = null


    private var toolsCardList: List<ModelMenu> = mutableListOf()
    private var recoveryList: List<ModelMenu> = mutableListOf()
    private var statusList: List<ModelMenu> = mutableListOf()

    private var homeMenuAdapter: HomeMenuAdapter? = null
    private var statusMenuAdapter: StatuseMenuAdapter? = null
    private var toolsMenuAdapter: ToolsMenuAdapter? = null
    private var notificationListenerDialog: AlertDialog? = null
    private lateinit var binding: ScreenHomeBinding

    private var storagePermission =
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storagePermission33 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO
    )

    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager


    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //val data: Intent? = result.data
                if (isNotificationListenerEnable) {
                    if (notificationListenerDialog != null) {
                        notificationListenerDialog?.dismiss()
                        notificationListenerDialog?.cancel()
                        appSharedPreferences?.hSetFirstTime(false)
                        val intent = Intent(this@MainActivity, MainRecoverActivity::class.java)
                        intent.putExtra("is_new_user", true)
                        startActivity(intent)
                    }
                }
            }
        }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            exitAlertDialog()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appSharedPreferences = MyAppSharedPrefs(this)

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)

        createDirectory()
        askNotificationPermission()
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        recoveryList = MyAppDataUtils.setRecoveryData(this)
        statusList = MyAppDataUtils.setStatusMenu(this)
        toolsCardList = MyAppDataUtils.setToolsData(this)

        val assetManager = assets
        MyAppExcelUtils.handleAssetManager(assetManager)

        showPremium()
        loadAds()

        setupRecoveryRecycler()
        setupStatusRecycler()
        setupToolsCardRecycler()
        initClickListeners()



        setSupportActionBar(binding.appbarHome.toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appbarHome.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        navigationClickListener()
        binding.navView.itemIconTintList = null
    }

    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(binding.appbarHome.topAdsLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            binding.appbarHome.nativebanner,
            binding.appbarHome.shimmerViewContainer
        )
    }


    private fun navigationClickListener() {
        binding.navView.setNavigationItemSelectedListener { item: MenuItem ->

            when (item.itemId) {


                R.id.action_share -> {
                    shareApp()
                }

                R.id.action_rate -> {
                    rateUs()
                }


                R.id.action_family -> {
                    moreApps()
                }

                R.id.action_privacy -> {
                    goToPrivacyPolicy()
                }

                R.id.action_language -> {
                    goToChangeLanguage()
                }

                R.id.action_privacy_settings -> {
                    showConsentForm()
                }

                R.id.cp_filerecovert -> {
                    cpApps("https://play.google.com/store/apps/details?id=com.catchyapps.audiorecover")
                }

                R.id.cp_Translator -> {
                    cpApps("https://play.google.com/store/apps/details?id=com.catchyapps.phototranslator")
                }

            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun cpApps(playStoreUrl: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun goToChangeLanguage() {
        startActivity(
            Intent(this, ChangeLanguageActivity::class.java)
                .putExtra("fromHome", true)
        )
    }

    private fun goToPrivacyPolicy() {
        startActivity(
            Intent(
                this@MainActivity,
                PrivacyPolicyScreen::class.java
            )
        )
    }

    private fun moreApps() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:Catchy-Apps")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/developer?id=Catchy-Apps")
                )
            )
        }
    }

    private fun showConsentForm() {
        googleMobileAdsConsentManager.showPrivacyOptionsForm(this@MainActivity) { formError ->
            if (formError != null) {
                Toast.makeText(this@MainActivity, formError.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            buildString {
                append(getString(R.string.check_this_app))
                append(getString(R.string.play_url, packageName))
            }
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    private fun setupRecoveryRecycler() {
        homeMenuAdapter = HomeMenuAdapter(
            recoveryList,
            onClick = { menuItem ->
                when (menuItem.id) {
                    "chat" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "Chat")
                        )
                    }

                    "documents" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "documents")
                        )
                    }

                    "images" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "images")
                        )
                    }

                    "video" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "video")
                        )
                    }

                    "audio" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "audio")
                        )
                    }

                    "voice" -> {
                        startActivity(
                            Intent(this, MainRecoverActivity::class.java)
                                .putExtra("tab", "voice")
                        )
                    }

                }
            }
        )

        binding.appbarHome.recoveryRecyclerView.adapter = homeMenuAdapter
        binding.appbarHome.recoveryRecyclerView.layoutManager = GridLayoutManager(
            this, 3
        )
    }


    private fun createDirectory() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            checkDocFolder()
        }
    }

    private fun rateUs() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,

                Uri.parse(getString(R.string.play_url, packageName))
            )
        )
    }

    private fun checkDocFolder() {

        val dir = File(
            Environment.getExternalStorageDirectory().toString() + "/"
                    + MyAppUtils.ROOT_FOLDER
        )
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
            Timber.d("Created")
        }
        if (isDirectoryCreated) {
            Timber.d("Already Created")
        }
    }


    private fun showPremium() {
        if (appSharedPreferences?.isPremium == false) {
            goToPremium()
        }
    }

    private fun setupStatusRecycler() {

        statusMenuAdapter = StatuseMenuAdapter(
            statusList,
            onClick = { menuItem ->
                when (menuItem.id) {
                    "statusImages" -> {
                        checkStatusSaverPermissions(0)
                    }

                    "statusVideos" -> {
                        checkStatusSaverPermissions(1)
                    }

                    "savedstatus" -> {
                        checkStatusSaverPermissions(3)
                    }

                }
            }
        )

        binding.appbarHome.statusRecyclerView.adapter = statusMenuAdapter
        binding.appbarHome.statusRecyclerView.layoutManager = GridLayoutManager(
            this,
            3
        )
    }

    private fun setupToolsCardRecycler() {

        toolsMenuAdapter = ToolsMenuAdapter(
            toolsCardList,
            onClick = { menuItem ->
                when (menuItem.id) {
                    "whatsweb" -> {
                        goToWhatsWeb()
                    }

                    "textStyle" -> {
                        goToTextStyle()
                    }

                    "textRepeater" -> {
                        goToTextRepeater()
                    }

                    "directchat" -> {
                        goToDirectChatActivity()
                    }

                    "whatscleaner" -> {
                        goToWhatsCleaner()
                    }

                    "quotations" -> {
                        gotoMotivationalQuotes()
                    }

                    "texttoemoji" -> {
                        gotoTextToEmoji()
                    }

                    "stickers" -> {
                        gotoStickers()
                    }

                    "ascii" -> {
                        gotoAscii()
                    }


                }
            }
        )

        binding.appbarHome.moreToolsRecyclerView.adapter = toolsMenuAdapter
        binding.appbarHome.moreToolsRecyclerView.layoutManager = GridLayoutManager(
            this,
            4
        )

    }

    private fun goToDirectChatActivity() {
        startActivity(
            Intent(
                this@MainActivity,
                DirectChatScreenActivity::class.java
            )
        )
        ShowInterstitial.showAdmobInter(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        val mSearch = menu.findItem(R.id.actionSearch)
        mSearch.isVisible = false
        val settings = menu.findItem(R.id.action_settings)
        settings.isVisible = false

        val viewGift = menu.findItem(R.id.action_prem)
        val view = viewGift.actionView
        view?.setOnClickListener {
            showPremiumDialog()
        }

        return true
    }

    private fun showPremiumDialog() {
        goToPremium()
    }

    private fun goToPremium() {
        val intent = Intent(this, ActivityPremium::class.java)
        startActivity(intent)
    }


    private fun exitAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.layout_custom_dialog_alert_, null)
        builder.setView(view)
        val btnYes: Button = view.findViewById(R.id.btn_positive)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnNo: Button = view.findViewById(R.id.btn_negative)
        btnYes.setOnClickListener {
            dialog.dismiss()
            dialog.cancel()
            finishAffinity()
        }
        btnNo.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }


    private val isNotificationListenerEnable: Boolean
        get() {
            val contentResolver = this.contentResolver
            val enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName = this.packageName

            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(
                packageName
            ))
        }

    private fun showNotificationListenerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notification_listener_service))
            .setMessage(R.string.we_need_your_permission_desc)
        builder.setPositiveButton(getString(R.string.goto_settings)) { dialog: DialogInterface, _: Int ->
            dialog.cancel()
            dialog.dismiss()
            notificationPermissionLauncher.launch(
                Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                )
            )
        }
        builder.setNegativeButton(getString(R.string.not_now)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        notificationListenerDialog = builder.create()
        notificationListenerDialog?.setCanceledOnTouchOutside(false)
        notificationListenerDialog?.show()
    }

    private fun checkStatusSaverPermissions(position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Permissions.check(
                this, storagePermission33,
                null /*rationale*/, null /*options*/, object : PermissionHandler() {
                    override fun onGranted() {
                        val intent = Intent(
                            this@MainActivity,
                            ActivityStatusMain::class.java
                        )
                        intent.putExtra("tab", position)
                        startActivity(intent)
                        ShowInterstitial.showAdmobInter(this@MainActivity)
                    }
                })
        } else {
            Permissions.check(
                this /*context*/,
                storagePermission,
                null /*rationale*/,
                null /*options*/,
                object : PermissionHandler() {
                    override fun onGranted() {
                        val intent = Intent(
                            this@MainActivity,
                            ActivityStatusMain::class.java
                        )
                        intent.putExtra("tab", position)
                        startActivity(intent)
                        ShowInterstitial.showAdmobInter(this@MainActivity)
                    }
                })
        }

    }


    private fun startIntentForRecover(name: String) {
        if (isNotificationListenerEnable) {
            appSharedPreferences?.hSetFirstTime(false)
            val intent = Intent(this@MainActivity, MainRecoverActivity::class.java)
            intent.putExtra("tab", name)
            startActivity(intent)
            ShowInterstitial.showAdmobInter(this)
        } else {
            showNotificationListenerDialog()
        }

    }

    private fun goToTextRepeater() {
        startActivity(Intent(this@MainActivity, TextRepeaterScreen::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun goToSavedCollections() {
        startActivity(
            Intent(
                this@MainActivity,
                ActivityStatusSavedCollections::class.java
            )
        )
        ShowInterstitial.showAdmobInter(this)
    }

    private fun goToTextStyle() {
        startActivity(Intent(this@MainActivity, StylishTextActivity::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun gotoMotivationalQuotes() {
        startActivity(Intent(this@MainActivity, ActivityQuotationsScreen::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun gotoAscii() {
        startActivity(
            Intent(
                this@MainActivity,
                com.catchyapps.whatsdelete.appactivities.activityasciifaces.ActivityAsciiFaces::class.java
            )
        )
        ShowInterstitial.showAdmobInter(this)
    }

    private fun gotoTextToEmoji() {
        startActivity(Intent(this@MainActivity, TextToEmojiScreen::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun gotoStickers() {
        startActivity(Intent(this@MainActivity, ActivityStickersScreen::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun goToWhatsWeb() {
        startActivity(Intent(this@MainActivity, WhatsWebScreen::class.java))
        ShowInterstitial.showAdmobInter(this)
    }

    private fun goToWhatsCleaner() {
        startActivity(Intent(this@MainActivity, WACleanerScreen::class.java))
        ShowInterstitial.showInter(this)
    }


    private fun initClickListeners() {

        /*   binding.appbarHome.dirChat.etnumber.addTextChangedListener(object : TextWatcher {
               override fun beforeTextChanged(
                   charSequence: CharSequence?,
                   start: Int,
                   count: Int,
                   after: Int
               ) {
               }

               override fun onTextChanged(
                   charSequence: CharSequence?,
                   start: Int,
                   before: Int,
                   count: Int
               ) {
               }

               override fun afterTextChanged(editable: Editable?) {
                   if (editable.toString().isNotEmpty()) {
                       binding.appbarHome.dirChat.btnDirectChat.isEnabled = true
                       binding.appbarHome.dirChat.btnDirectChat.setBackgroundResource(R.drawable.btn_direct_chat_background)
                   } else {

                       binding.appbarHome.dirChat.btnDirectChat.setBackgroundResource(R.drawable.gray_btn_background)
                   }
               }
           })

           binding.appbarHome.dirChat.tvcountrycode.setOnClickListener {
               val fragment = FragmentCountryCodeSelection.newInstance()
               fragment.show(supportFragmentManager, "dialog")
           }
           binding.appbarHome.dirChat.btnDirectChat.setOnClickListener {
               when {
                   binding.appbarHome.dirChat.etnumber.text.toString().isEmpty() -> {
                       binding.appbarHome.dirChat.etnumber.error = getString(R.string.enter_number)

                   }

                   binding.appbarHome.dirChat.tvcountrycode.text.toString().isEmpty() -> {
                       binding.appbarHome.dirChat.tvcountrycode.error = getString(R.string.select_country_code)
                   }

                   else -> {
                       number =
                           binding.appbarHome.dirChat.tvcountrycode.text.toString()
                               .plus(binding.appbarHome.dirChat.etnumber.text.toString())
                       openWhatsApp(number)
                   }
               }
           }
           if (countryZipCode().isNotEmpty())  binding.appbarHome.dirChat.tvcountrycode.text =
               countryZipCode() else  binding.appbarHome.dirChat.tvcountrycode.setText(R.string._922)*/
    }

    private fun countryZipCode(): String {
        val countryID: String
        var countryZipCode = ""
        val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        countryID = manager.simCountryIso.uppercase(Locale.getDefault())
        val rl = this.resources.getStringArray(R.array.CountryCodes)
        for (s in rl) {
            val g = s.split(",").toTypedArray()
            if (g[1].trim { it <= ' ' } == countryID.trim { it <= ' ' }) {
                countryZipCode = g[0]
                break
            }
        }
        return countryZipCode
    }

    private fun openWhatsApp(smsNumber: String?) {
        val i = Intent(Intent.ACTION_VIEW)
        try {
            val url = "https://wa.me/$smsNumber"
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.whatsapp_not_found), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun setMyName(string: String?) {
        // binding.appbarHome.dirChat.tvcountrycode.text = string
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "PERMISSION_GRANTED")
            } else {
                Log.e(TAG, "NO_PERMISSION")
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT)
                .show()
        } else {

            Snackbar.make(
                binding.navViewHead,
                String.format(
                    String.format(
                        getString(R.string.turn_On_Notification),
                        getString(R.string.app_name)
                    )
                ),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.goto_settings)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(settingsIntent)
                }
            }.show()
        }
    }

}