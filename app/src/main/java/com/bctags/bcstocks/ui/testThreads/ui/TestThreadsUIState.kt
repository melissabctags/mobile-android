package com.bctags.bcstocks.ui.testThreads.ui

sealed class TestThreadsUIState {

    object Loading : TestThreadsUIState()
    data class Success(var numSubscribers: Int) : TestThreadsUIState()

    data class Error(val msg: String) : TestThreadsUIState()
}