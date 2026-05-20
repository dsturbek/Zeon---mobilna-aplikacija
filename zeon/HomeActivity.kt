package com.example.zeon

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.zeon.presentation.ui.adapters.HomePagerAdapter
import com.example.zeon.presentation.ui.fragments.ActivityCalendarFragment
import com.example.zeon.presentation.ui.fragments.ChatFragment
import com.example.zeon.presentation.ui.fragments.FoodMainFragment
import com.example.zeon.presentation.ui.fragments.HomeFragment
import com.example.zeon.presentation.ui.fragments.PlanFragment
import com.example.zeon.presentation.ui.fragments.ProfileFragment
import com.example.zeon.helpers.NotificationHelper
import com.example.zeon.helpers.WorkoutReminderManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "HomeActivity"
    }

    lateinit var navigation: TabLayout
    lateinit var viewPagerHome: ViewPager2

    private lateinit var fragmentContainer: FrameLayout

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission GRANTED")
            startWorkoutReminder()
        } else {
            Log.w(TAG, "Notification permission DENIED - notifications will not work")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navigation = findViewById(R.id.navigation)
        viewPagerHome = findViewById(R.id.viewpager_home)
        fragmentContainer = findViewById(R.id.fragment_container_home)

        setupNavigation()

        initWorkoutReminder()
    }

    private fun initWorkoutReminder() {
        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startWorkoutReminder()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startWorkoutReminder()
        }
    }

    private fun startWorkoutReminder() {
        // TODO: Zamijeniti hardkodirani clientId s pravim nakon integracije login sustava
        val clientId = 1
        Log.d(TAG, "Starting workout reminder for clientId: $clientId")

        WorkoutReminderManager.startPeriodicReminder(this, clientId)

        // TODO: Ukloniti nakon testiranja - jednokratno pokretanje za test
        WorkoutReminderManager.checkNow(this, clientId)
    }

    private fun setupNavigation(){
        val homePagerAdapter = HomePagerAdapter(supportFragmentManager, lifecycle)
        homePagerAdapter.addFragment(
            HomePagerAdapter.FragmentItem(
                R.string.home_fragment,
                R.drawable.baseline_home_24,
                HomeFragment::class
            )
        )
        homePagerAdapter.addFragment(
            HomePagerAdapter.FragmentItem(
                R.string.activity_calendar_fragment,
                R.drawable.baseline_calendar_month_24,
                PlanFragment::class
            )
        )
        homePagerAdapter.addFragment(
            HomePagerAdapter.FragmentItem(
                R.string.chat_fragment,
                R.drawable.outline_3p_24,
                ChatFragment::class
            )
        )
        homePagerAdapter.addFragment(
            HomePagerAdapter.FragmentItem(
                R.string.food_fragment,
                R.drawable.outline_cake_add_24,
                FoodMainFragment::class
            )
        )

        homePagerAdapter.addFragment(
            HomePagerAdapter.FragmentItem(
                R.string.profile_fragment,
                R.drawable.outline_accessibility_24,
                ProfileFragment::class
            )
        )


        viewPagerHome.adapter = homePagerAdapter

        TabLayoutMediator(navigation, viewPagerHome) {
                tab, position ->
            tab.setText(homePagerAdapter.fragmentItems[position].titleRes)
            tab.setIcon(homePagerAdapter.fragmentItems[position].iconRes)
        }.attach()
    }

    fun openFragment(fragment: Fragment) {
        navigation.visibility = View.GONE
        viewPagerHome.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_home, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun returnToViewPager(){
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        navigation.visibility = View.VISIBLE
        viewPagerHome.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()

            if (supportFragmentManager.backStackEntryCount == 0) {
                navigation.visibility = View.VISIBLE
                viewPagerHome.visibility = View.VISIBLE
                fragmentContainer.visibility = View.GONE
            }
        } else {
            super.onBackPressed()
        }
    }
}