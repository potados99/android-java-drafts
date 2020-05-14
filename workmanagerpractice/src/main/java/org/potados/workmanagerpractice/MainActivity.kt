package org.potados.workmanagerpractice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.view.*
import org.potados.workmanagerpractice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initBinding(binding)
        initView(binding.root)

        startTask()
    }

    private fun initBinding(binding: ActivityMainBinding) {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        with(binding) {
            vm = viewModel
            lifecycleOwner = this@MainActivity
        }
    }

    private fun initView(root: View) {
        // root.myTextView
    }

    private fun startTask() {
        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val progress = intent?.getIntExtra("PROGRESS", 0) ?: return

                viewModel.setProgress(progress)
            }
        }
        val filter = IntentFilter("GALLERY_PROGRESS_REPORT")
        registerReceiver(receiver, filter)

        SomeWorkingClass().doYourJob(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }
}
