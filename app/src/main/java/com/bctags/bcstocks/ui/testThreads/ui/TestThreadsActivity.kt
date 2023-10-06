package com.bctags.bcstocks.ui.testThreads.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bctags.bcstocks.databinding.ActivityTestThreadsBinding
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.launch

class TestThreadsActivity : DrawerBaseActivity() {

    private lateinit var binding: ActivityTestThreadsBinding

    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestThreadsBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        viewModel.example()

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.uiState.collect{ uiState->
                    when(uiState){
                        is TestThreadsUIState.Error -> {}
                        TestThreadsUIState.Loading -> {}
                        is TestThreadsUIState.Success -> {}
                    }
                }
            }
        }
        viewModel.example3()
    }















}