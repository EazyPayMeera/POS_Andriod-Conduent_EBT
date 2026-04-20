package com.eazypaytech.pos.domain.model

data class DashboardItemList(val text: String,
                             val iconResId: Int,
                             val event: String,
                             val isPassword: Boolean = false)