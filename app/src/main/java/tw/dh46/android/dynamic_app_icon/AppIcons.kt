package tw.dh46.android.dynamic_app_icon

/**
 *  Created by DanielHuang on 2023/11/21
 */
enum class AppIcons(val alias: String, val iconResId: Int, var isSelected: Boolean) {
    Default("MainActivityDefault", R.mipmap.ic_launcher, false),
    ActivatedDefault("MainActivityActivatedDefault", R.mipmap.ic_launcher, false),
    Blue("MainActivityBlue", R.mipmap.ic_launcher_blue, false),
    Red("MainActivityRed", R.mipmap.ic_launcher_red, false),
    Yellow("MainActivityYellow", R.mipmap.ic_launcher_yellow, false)
}

val appIconsList =
    listOf<AppIcons>(AppIcons.ActivatedDefault, AppIcons.Blue, AppIcons.Red, AppIcons.Yellow)