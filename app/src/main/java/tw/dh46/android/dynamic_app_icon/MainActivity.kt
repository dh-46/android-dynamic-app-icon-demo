package tw.dh46.android.dynamic_app_icon

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
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
        initViews()
    }

    override fun onStart() {
        super.onStart()
        updateRvIconsSelection()
    }

    // -------------------------------------------------------------

    private fun initViews() {
        binding.btnFeature.setOnClickListener {
            val textResId = if (binding.btnFeature.text == getString(R.string.activate)) {
                R.string.deactivate
            } else {
                R.string.activate
            }
            binding.btnFeature.setText(textResId)
        }
    }

    private fun initRvIcons() {
        binding.rvIconList.layoutManager = GridLayoutManager(this, 3)
        val adapter = IconItemAdapter(AppIcons.entries) { it, index ->
            it.isSelected = !it.isSelected

            if (it.isSelected) {
                AppIcons.entries.forEach {
                    it.isSelected = false
                    setAliasComponentEnabled(it.alias, false)
                }

                setAliasComponentEnabled(it.alias, true)
            }
        }
        binding.rvIconList.adapter = adapter
    }

    // -------------------------------------------------------------

    private fun updateRvIconsSelection() {
        getPackageInfo().activities.forEach { activityInfo ->
            val isEnabled = isComponentEnabled(activityInfo)

            if (isEnabled) {
                AppIcons.entries.forEach {

                    if (activityInfo.name == "$packageName.${it.alias}") {
                        it.isSelected = true
                        return
                    }
                }
            }
        }

        binding.rvIconList.adapter?.notifyDataSetChanged()
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

    private fun getPackageInfo(): PackageInfo {
        return packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
        )
    }


    private fun setAliasComponentEnabled(alias: String, enable: Boolean) {
        val newState = if (enable) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            ComponentName(packageName, "$packageName.$alias"),
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}