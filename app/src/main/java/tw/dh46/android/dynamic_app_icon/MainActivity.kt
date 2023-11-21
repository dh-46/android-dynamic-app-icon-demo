package tw.dh46.android.dynamic_app_icon

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
            binding.rvIconList.adapter?.notifyItemChanged(index)
        }
        binding.rvIconList.adapter = adapter
    }
}