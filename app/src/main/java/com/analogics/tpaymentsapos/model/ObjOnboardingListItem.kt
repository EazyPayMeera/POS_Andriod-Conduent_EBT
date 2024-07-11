package com.analogics.tpaymentsapos.model

import androidx.compose.ui.res.painterResource
import com.analogics.tpaymentsapos.R

data class ObjOnboardingListItem(
    var image:Int=  R.drawable.group_360,
    var headNote:String="",
    var subNote:String="",
    var isIndicatorShow:Boolean=true)
