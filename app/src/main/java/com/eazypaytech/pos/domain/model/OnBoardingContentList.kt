package com.eazypaytech.pos.domain.model

import com.eazypaytech.pos.R

data class OnBoardingContentList(
    var image:Int=  R.drawable.onboarding1,
    var headNote:String="",
    var subNote:String="",
    var isIndicatorShow:Boolean=true)