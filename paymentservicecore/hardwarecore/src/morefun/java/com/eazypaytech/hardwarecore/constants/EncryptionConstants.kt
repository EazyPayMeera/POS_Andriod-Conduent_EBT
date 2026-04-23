package com.eazypaytech.tpaymentcore.constants

import com.morefun.yapi.device.pinpad.DukptCalcObj
import com.morefun.yapi.device.pinpad.DukptLoadObj

object EncryptionConstants {

    /* Key ID */
    val KEY_INDEX_MAIN_KEY = DukptCalcObj.DukptKeyIndexEnum.KEY_INDEX_1
    val KEY_INDEX_DATA_KEY = DukptCalcObj.DukptKeyIndexEnum.KEY_INDEX_5


    /* Master Session Key Type */
    const val MS_KEY_TYPE_PIN = 2

}
