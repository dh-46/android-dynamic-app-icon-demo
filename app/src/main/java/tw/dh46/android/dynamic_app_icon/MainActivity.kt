package tw.dh46.android.dynamic_app_icon

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import tw.dh46.android.dynamic_app_icon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var appIconManager: AppIconManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        appIconManager = AppIconManager(
            this,
            packageManager
        )

        initRvIcons()
        initBtnFeature()
    }

    override fun onStart() {
        super.onStart()
        notifyIconListChanged()
    }

    // -------------------------------------------------------------

    private fun initBtnFeature() {
        val textResId = if (appIconManager.isFeatureActivated()) {
            R.string.deactivate
        } else {
            R.string.activate
        }
        binding.btnFeature.setText(textResId)

        binding.btnFeature.setOnClickListener {

            if (binding.btnFeature.text == getString(R.string.activate)) {
                appIconManager.activateFeature()
                binding.btnFeature.setText(R.string.deactivate)
            } else {
                appIconManager.deactivateFeature()
                binding.btnFeature.setText(R.string.activate)
            }

            notifyIconListChanged()
        }
    }

    private fun initRvIcons() {
        binding.rvIconList.layoutManager = GridLayoutManager(this, 3)
        val adapter = IconItemAdapter(appIconManager.getLatestIconOptions()) { it, index ->

            try {
                appIconManager.setActiveAppIcon(it)
                notifyIconListChanged()
            } catch (e: Exception) {
                Toast.makeText(this, R.string.error_feature_not_activated, Toast.LENGTH_SHORT)
                    .show()
            }

        }
        binding.rvIconList.adapter = adapter
    }

    private fun notifyIconListChanged() {
        binding.rvIconList.adapter?.notifyDataSetChanged()
    }
}