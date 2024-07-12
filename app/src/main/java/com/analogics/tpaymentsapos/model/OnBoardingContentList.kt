package com.analogics.tpaymentsapos.model

import com.analogics.tpaymentsapos.R

data class OnBoardingContentList(
    var image:Int=  R.drawable.group_360,
    var headNote:String="",
    var subNote:String="",
    var isIndicatorShow:Boolean=true)
