package com.rld.justlisten.viewmodel.donation

import com.rld.justlisten.viewmodel.BaseScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DonationViewModel : BaseScreenViewModel() {

    private val _donationState = MutableStateFlow(DonationScreenState())
    val donationState: StateFlow<DonationScreenState> = _donationState.asStateFlow()
}

data class DonationScreenState(
    val isLoading: Boolean = false,
)

