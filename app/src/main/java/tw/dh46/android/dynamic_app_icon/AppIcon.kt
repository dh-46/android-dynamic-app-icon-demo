package tw.dh46.android.dynamic_app_icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager

/**
 *  Created by DanielHuang on 2023/11/21
 */
sealed class AppIcon(val alias: String, val iconResId: Int, var isEnable: Boolean) {
    data object BuiltIn : AppIcon("MainActivityBuiltIn", R.mipmap.ic_launcher, true)
    data object Default : AppIcon("MainActivityDefault", R.mipmap.ic_launcher, false)
    data object Blue : AppIcon("MainActivityBlue", R.mipmap.ic_launcher_blue, false)
    data object Red : AppIcon("MainActivityRed", R.mipmap.ic_launcher_red, false)
    data object Yellow : AppIcon("MainActivityYellow", R.mipmap.ic_launcher_yellow, false)
}

private val appIconOptions = listOf(AppIcon.Default, AppIcon.Blue, AppIcon.Red, AppIcon.Yellow)

/**
 * App icon manager
 *
 * @property context
 * @property packageManager
 * @property buildInAppIcon 安裝當下預設的 Icon
 * @property defaultAppIcon 啟用更換 Icon 後的預設 Icon
 * @property targetActivity App 進入點的 Activity 名稱，也是 activity-alias 的 targetActivity
 * @constructor Create empty App icon manager
 */
class AppIconManager(
    private val context: Context,
    private val packageManager: PackageManager = context.packageManager,
    private val buildInAppIcon: AppIcon = AppIcon.BuiltIn,
    private val defaultAppIcon: AppIcon = AppIcon.Default,
    private val targetActivity: String = "MainActivity"
) {
    /**
     * 啟用更換 Icon 功能
     */
    fun activateFeature() {
        setAliasComponentState(defaultAppIcon.alias, true)
        setAliasComponentState(buildInAppIcon.alias, false)
    }

    /**
     * 停用更換 Icon 功能
     */
    fun deactivateFeature() {
        setActiveAppIcon(buildInAppIcon)
    }

    /**
     * 設定目前啟用的 AppIcon
     *
     * @param appIcon
     */
    fun setActiveAppIcon(appIcon: AppIcon) {
        // 功能啟用後才能變換 Icon
        checkFeatureActivated()

        // 停用其他選項
        disableOtherIconOptions(appIcon)

        // 啟用選中的選項
        enableIcon(appIcon)
    }

    private fun enableIcon(appIcon: AppIcon) {
        appIcon.isEnable = true
        setAliasComponentState(appIcon.alias, true)
    }

    /**
     * 停用其他選項
     *
     * @param appIcon
     */
    private fun disableOtherIconOptions(appIcon: AppIcon) {
        getLatestIconOptions().filterNot {
            it == appIcon
        }.forEach {
            it.isEnable = false
            setAliasComponentState(it.alias, false)
        }
    }

    /**
     * 檢查是否啟用
     *
     */
    private fun checkFeatureActivated() {
        if (!isFeatureActivated()) throw IllegalStateException("Feature is not activated!")
    }

    /**
     * 檢查 activity-alias: MainActivityDefault 是否被停用 (Disabled)
     * 來辨識是否已啟用更換圖示的功能
     *
     * @return
     */
    fun isFeatureActivated(): Boolean {
        return packageManager.getComponentEnabledSetting(
            createComponentName(buildInAppIcon.alias),
        ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    }

    /**
     * 取得最新狀態的 Icon 選項
     *
     * @return
     */
    fun getLatestIconOptions(): List<AppIcon> {
        getLauncherActivityInfoList().forEach { activityInfo ->
            val isEnabled = isComponentEnabled(activityInfo)

            if (isEnabled) {
                appIconOptions.forEach {
                    if (activityInfo.name == "${context.packageName}.${it.alias}") {
                        it.isEnable = true
                    }
                }
            }
        }

        return appIconOptions
    }

    // ----------------------------------------------------

    private fun setAliasComponentState(alias: String, enable: Boolean) {
        // 檢查是否有非法 alias
        require(buildInAppIcon.alias == alias || appIconOptions.any { it.alias == alias }) { "Invalid alias: $alias" }

        val newState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            if (alias == buildInAppIcon.alias) {
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
        ComponentName(context, "${context.packageName}.$alias")

    private fun isComponentEnabled(activityInfo: ActivityInfo): Boolean {
        val state =
            packageManager.getComponentEnabledSetting(ComponentName(context, activityInfo.name))

        val isEnabled = if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            activityInfo.enabled
        } else {
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        return isEnabled
    }

    private fun getLauncherActivityInfoList(targetActivityClassName: String = targetActivity): List<ActivityInfo> {
        return packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
        ).activities.filter {
            it.name.contains(targetActivityClassName)
        }
    }
}