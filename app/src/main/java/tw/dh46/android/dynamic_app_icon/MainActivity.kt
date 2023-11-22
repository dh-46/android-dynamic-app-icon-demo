package tw.dh46.android.dynamic_app_icon

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import tw.dh46.android.dynamic_app_icon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initRvIcons()
        initBtnFeature()
    }

    override fun onStart() {
        super.onStart()
        updateRvIconsSelection()
    }

    // -------------------------------------------------------------

    private fun initBtnFeature() {
        val textResId = if (isFeatureActivated()) {
            R.string.deactivate
        } else {
            R.string.activate
        }
        binding.btnFeature.setText(textResId)

        binding.btnFeature.setOnClickListener {

            if (binding.btnFeature.text == getString(R.string.activate)) {

                setAliasComponentEnabled(AppIcons.ActivatedDefault.alias, true)
                setAliasComponentEnabled(AppIcons.Default.alias, false)

                binding.btnFeature.setText(R.string.deactivate)
            } else {

                getLauncherActivityInfoList().forEach {
                    if (it.name.contains("MainActivity")) {
                        packageManager.setComponentEnabledSetting(
                            ComponentName(this, it.name),
                            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }

                binding.btnFeature.setText(R.string.activate)
            }

            updateRvIconsSelection()
        }
    }

    private fun initRvIcons() {
        binding.rvIconList.layoutManager = GridLayoutManager(this, 3)
        val adapter = IconItemAdapter(appIconsList) { it, index ->
            // 啟用後才能變換 Icon
            if (!isFeatureActivated()) {
                Toast.makeText(this, R.string.error_feature_not_activated, Toast.LENGTH_SHORT)
                    .show()
                return@IconItemAdapter
            }

            it.isSelected = !it.isSelected

            if (it.isSelected) {
                appIconsList.forEach {
                    it.isSelected = false
                    setAliasComponentEnabled(it.alias, false)
                }

                setAliasComponentEnabled(it.alias, true)
            }

            updateRvIconsSelection()
        }
        binding.rvIconList.adapter = adapter
    }

    private fun updateRvIconsSelection() {
        if (!isFeatureActivated()) {
            appIconsList.forEach {
                it.isSelected = false
            }
        }

        getLauncherActivityInfoList().forEach { activityInfo ->
            val isEnabled = isComponentEnabled(activityInfo)

            if (isEnabled) {
                appIconsList.forEach {
                    if (activityInfo.name == "$packageName.${it.alias}") {
                        it.isSelected = true
                    }
                }
            }
        }

        binding.rvIconList.adapter?.notifyDataSetChanged()
    }

    // -------------------------------------------------------------

    /**
     * 檢查 activity-alias: MainActivityDefault 是否被停用 (Disabled)
     * 來辨識是否已啟用更換圖示的功能
     *
     * @return
     */
    private fun isFeatureActivated(): Boolean {
        return packageManager.getComponentEnabledSetting(
            createComponentName(AppIcons.Default.alias),
        ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    /**
     * 取得程式進入點 (MainActivity) 與其相關的 activity-alias 的 ActivityInfo
     *
     * @param targetActivityClassName
     * @return
     */
    private fun getLauncherActivityInfoList(targetActivityClassName: String = "MainActivity"): List<ActivityInfo> {
        return packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
        ).activities.filter {
            it.name.contains(targetActivityClassName)
        }
    }

    private fun isComponentEnabled(activityInfo: ActivityInfo): Boolean {
        val state =
            packageManager.getComponentEnabledSetting(ComponentName(this, activityInfo.name))

        val isEnabled = if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            activityInfo.enabled
        } else {
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        return isEnabled
    }


    private fun setAliasComponentEnabled(alias: String, enable: Boolean) {
        val newState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            if (alias == AppIcons.Default.alias) {
                // 如果是安裝預設選項 STATE 要設為 DISABLED 停用
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                // 樣式 Icon 切換，關閉是設為 DEFAULT (避免切換 Icon 後 App 必須關閉)
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            }
        }

        packageManager.setComponentEnabledSetting(
            createComponentName(alias),
            newState,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun createComponentName(alias: String) =
        ComponentName(packageName, "$packageName.$alias")
}