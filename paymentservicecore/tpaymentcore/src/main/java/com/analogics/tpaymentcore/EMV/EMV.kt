package com.analogics.tpaymentcore.EMV

import android.content.Context
import android.device.SEManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IInputActionListener
import android.text.TextUtils
import android.util.Log
import com.analogics.tpaymentcore.listener.IPaymentSDKListener
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import java.util.Hashtable
import java.util.Locale

class EMV {
    companion object : EmvListener, PinInputListener {
        lateinit var iPaymentSDKListener: IPaymentSDKListener
        fun initialize(context: Context) {
            Thread {
                try {
                    EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.CLEAR, null)
                    EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.CLEAR, null) //
                    //Update the parameters required in actual use
                    initEMV_AID_CAPK()
                    //init_NfcAid_CAPK()

                    EmvNfcKernelApi.getInstance().updateTerminalParamters(
                        ContantPara.CardSlot.ICC,
                        "9F4E1755524F564F5F544553545F4D454348414E545F4E414D459F150211229F160F1234567890123451234567890123459F1C0831323334353637389F4005F000F0A0019F1A0206829F3303E068009F3501225F360102DF020101DF030101DF050100" + "9F1E08" + "1122334455667788"
                    ) //DF02---random trans select enable  DF03--Except file check enable DF04--Support SM DF05-- Valocity Check enable
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        fun startPayment(context: Context, iPaymentSDKListener: IPaymentSDKListener) {
            Thread {
                try {
                    this.iPaymentSDKListener = iPaymentSDKListener
                    val data = Hashtable<String, Any>()
                    data["checkCardMode"] = ContantPara.CheckCardMode.INSERT_OR_TAP //
                    data["currencyCode"] = "682" //682
                    data["emvOption"] = ContantPara.EmvOption.START // START_WITH_FORCE_ONLINE
                    data["amount"] = "0.01"
                    data["cashbackAmount"] = "0"
                    data["checkCardTimeout"] = "30" // Check Card time out .Second
                    data["transactionType"] = "00" //00-goods 01-cash 09-cashback 20-refund
                    data["isEnterAmtAfterReadRecord"] = false
                    data["FallbackSwitch"] = "0" //0- close fallback 1-open fallback
                    data["supportDRL"] = true // support Visa DRL?
                    EmvNfcKernelApi.getInstance().setContext(context)
                    EmvNfcKernelApi.getInstance().setListener(this)
                    EmvNfcKernelApi.getInstance().startKernel(data)
                    //EmvNfcKernelApi.getInstance().getEMVLibVers(ContantPara.CardSlot.ICC)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }

        override fun onRequestSetAmount() {
            Log.d("EMV_APP", "Request Amount:")

//            Log.e(TAG, TAG + "===onRequestSetAmount");
            EmvNfcKernelApi.getInstance().setAmountEx(1L, 0L)
        }

        override fun onReturnCheckCardResult(
            p0: ContantPara.CheckCardResult?,
            p1: Hashtable<String, String>?
        ) {
            Log.d("EMV_APP", "Check Card Result:" + p0.toString())
            Log.d("EMV_APP", "Check Card List:" + p1.toString())
            iPaymentSDKListener.onTPaymentDisplayMessage("Card Detected")
        }

        override fun onRequestSelectApplication(p0: ArrayList<String>?) {
            Log.d("EMV_APP", "Select Applications:" + p0.toString())
        }

        override fun onRequestPinEntry(p0: ContantPara.PinEntrySource?) {
            Log.d("EMV_APP", "Online PIN Prompt:" + p0.toString())
            //EmvNfcKernelApi.getInstance().sendPinEntry()
            //EmvNfcKernelApi.getInstance().bypassPinEntry()
            iPaymentSDKListener.onTPaymentDisplayMessage("")
            if (p0 == ContantPara.PinEntrySource.KEYPAD) {
                emv_proc_onlinePin(true)
                Log.i("EMV_APP", "MainActivity  emv_proc_onlinePin over")
            }
        }

        override fun onRequestOfflinePinEntry(p0: ContantPara.PinEntrySource?, p1: Int) {
            Log.d("EMV_APP", "Offline PIN Prompt:" + p0.toString())
        }

        override fun onRequestConfirmCardno() {
            EmvNfcKernelApi.getInstance().sendConfirmCardnoResult(true)
        }

        override fun onRequestFinalConfirm() {
            EmvNfcKernelApi.getInstance().sendFinalConfirmResult(true)
        }

        override fun onRequestOnlineProcess(p0: String?, p1: String?) {
            Log.d("EMV_APP", "Process Online:" + p0.toString() + "\n" + p1?.toString())
            EmvNfcKernelApi.getInstance().sendOnlineProcessResult(true, "8A023030")
        }

        override fun onReturnBatchData(p0: String?) {
            Log.d("EMV_APP", "Batch Data:" + p0.toString())
        }

        override fun onReturnTransactionResult(p0: ContantPara.TransactionResult?) {
            Log.d("EMV_APP", "Transaction Result:" + p0.toString())
            Log.d("EMV_APP", "TLV Data:" + EmvNfcKernelApi.getInstance().GetField55ForSAMA())
            if(p0==ContantPara.TransactionResult.ONLINE_APPROVAL || p0==ContantPara.TransactionResult.OFFLINE_APPROVAL)
                iPaymentSDKListener.onTPaymentSDKHandler("SUCCESS")
            else
                iPaymentSDKListener.onTPaymentSDKHandler("FAILURE")
        }

        override fun onRequestDisplayText(p0: ContantPara.DisplayText?) {
            Log.d("EMV_APP", "***** DISPLAY *****\n" + p0.toString() + "*******************")
        }

        override fun onRequestOfflinePINVerify(
            p0: ContantPara.PinEntrySource?,
            p1: Int,
            p2: Bundle?
        ) {
            Log.d("EMV_APP", "Offline PIN Verify:" + p0.toString())
            if (p0 == ContantPara.PinEntrySource.KEYPAD) { //use in os 8.0 or above
                //pinEntryType 0-offline plain pin ,1-offline encrypt pin
                val pinTryTimes: Int = EmvNfcKernelApi.getInstance().getOfflinePinTryTimes()
                p2?.putInt("PinTryTimes", pinTryTimes)
                p2?.putBoolean("isFirstTime", true)
                if (pinTryTimes == 1) proc_offlinePin(p1, true, p2!!)
                else {
                    proc_offlinePin(p1, false, p2!!)
                }
            }
/*
            if (p0 == ContantPara.PinEntrySource.KEYPAD)
                promptPin(null, false, 3, null, false);
*/
        }

        override fun onReturnIssuerScriptResult(p0: ContantPara.IssuerScriptResult?, p1: String?) {
            Log.d("EMV_APP", "Issuer Script Result:" + p0.toString())
        }

        override fun onNFCrequestTipsConfirm(p0: ContantPara.NfcTipMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Request Tip:" + p0.toString())
        }

        override fun onReturnNfcCardData(p0: Hashtable<String, String>?) {
            Log.d("EMV_APP", "NFC Card Data:" + p0.toString())
        }

        override fun onNFCrequestOnline() {
            Log.d("EMV_APP", "NFC Process Online:")
        }

        override fun onNFCrequestImportPin(p0: Int, p1: Int, p2: String?) {
            Log.d("EMV_APP", "NFC Import PIN:" + p2.toString())

//            Log.e(TAG, TAG + "===onNFCrequestImportPin, type:" + type + ", lasttimeFlag:" + lasttimeFlag + ", amt:" + amt);
            EmvNfcKernelApi.getInstance().sendPinEntry()
        }

        override fun onNFCTransResult(p0: ContantPara.NfcTransResult?) {
            Log.d("EMV_APP", "NFC Trans Result:" + p0.toString())
            if(p0==ContantPara.NfcTransResult.ONLINE_APPROVAL || p0==ContantPara.NfcTransResult.OFFLINE_APPROVAL)
                iPaymentSDKListener.onTPaymentSDKHandler("SUCCESS")
            else
                iPaymentSDKListener.onTPaymentSDKHandler("FAILURE")
        }

        override fun onNFCErrorInfor(p0: ContantPara.NfcErrMessageID?, p1: String?) {
            Log.d("EMV_APP", "NFC Trans Error:" + p0.toString())
        }


        fun initEMV_AID_CAPK() {
            addCAPK_visa()
            addCAPK_master()
            addCAPK_rupay()
            addEMV_AID_Paramters()
        }

        private fun addCAPK_visa() {
            val capk: Hashtable<String?, String?> = Hashtable<String?, String?>()

            capk["Rid"] = "A000000003"
            capk["Index"] = "08"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "53"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "BCD83721BE52CCCC4B6457321F22A7DC769F54EB8025913BE804D9EABBFA19B3D7C5D3CA658D768CAF57067EEC83C7E6E9F81D0586703ED9DDDADD20675D63424980B10EB364E81EB37DB40ED100344C928886FF4CCC37203EE6106D5B59D1AC102E2CD2D7AC17F4D96C398E5FD993ECB4FFDF79B17547FF9FA2AA8EEFD6CBDA124CBB17A0F8528146387135E226B005A474B9062FF264D2FF8EFA36814AA2950065B1B04C0A1AE9B2F69D4A4AA979D6CE95FEE9485ED0A03AEE9BD953E81CFD1EF6E814DFD3C2CE37AEFA38C1F9877371E91D6A5EB59FDEDF75D3325FA3CA66CDFBA0E57146CC789818FF06BE5FCC50ABD362AE4B80996D"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "09"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "57"
            capk["Exponent"] = "010001"
            capk["Modulus"] =
                "942B7F2BA5EA307312B63DF77C5243618ACC2002BD7ECB74D821FE7BDC78BF28F49F74190AD9B23B9713B140FFEC1FB429D93F56BDC7ADE4AC075D75532C1E590B21874C7952F29B8C0F0C1CE3AEEDC8DA25343123E71DCF86C6998E15F756E3"
            capk["Checksum"] = "429C954A3859CEF91295F663C963E582ED6EB253"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "92"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F"
            capk["Checksum"] = "429C954A3859CEF91295F663C963E582ED6EB253"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "94"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617"
            capk["Checksum"] = "C4A3C43CCF87327D136B804160E47D43B60E6E0F"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000003"
            capk["Index"] = "96"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D"
            capk["Checksum"] = "C63D0D8598AA7A5AA342FB80489C39A2A6E5A5F7"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)
        }

        private fun addCAPK_master() {
            val capk: Hashtable<String?, String?> = Hashtable<String?, String?>()
            capk["Rid"] = "A000000004"
            capk["Index"] = "04"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Rid"] = "A000000004"
            capk["Index"] = "05"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597"
            capk["Checksum"] = "EBFA0D5D06D8CE702DA3EAE890701D45E274C845"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "06"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F"
            capk["Checksum"] = "F910A1504D5FFB793D94F3B500765E1ABCAD72D9"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "EF"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "F1"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7"
            capk["Checksum"] = "D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "F3"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "F8"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "FA"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)

            capk["Index"] = "FE"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)
        }

        fun addCAPK_rupay() {
            val capk: Hashtable<String?, String?> = Hashtable<String?, String?>()
            capk["Rid"] = "A000000524"
            capk["Index"] = "05"
            capk["Exponent"] = "03"
            capk["Modulus"] =
                "C04E80180369898AAEF6EE7741EDED25239D765301614B5B41A008CA3009358D626D828BC5F1B1E04A2DC1367101266905D262003BE747FD231C9B0011F2F2B21BA8E4C0F4CA5E93ED9DBB2E92ABC450576A4EB59AD00DCA59C8BF3230E4B19D43452871C6215D837663310DF43CAEA1B9B08C1F500AF1B550F62E18D70EEE9E9475321BCD1799AB193E0BC849DACE892A0E6A1F42FE0786DB30345AE1A0E7E4C4B71640E03BFD2832C491A7D83F3B4EF4D388CDDBB748C2FD1D9D4A9BF52FC856CBA088D4B274846002C23CDA722C5CFF3B1F8218A1843B0426474BDC92F2F5E31FBF321CC17480AD069DF55381F2E601D5CBA7B871253F"
            capk["Checksum"] = "00000000000000000000000000000000000000"
            EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk)
        }

        fun addNSICCS_AID_Paramters() {
            val data = Hashtable<String, String>()
            data["CardType"] = "IcCard"
            data["aid"] = "A0000006021010"
            data["appVersion"] = "0100"
            //data.put("terminalFloorLimit", "00000000");
            //data.put("terminalFloorLimit", Funs.DecNumStrToHexNumStr("000000010000"));
            //data.put("contactTACDefault", "D84000A800");
            data["contactTACDefault"] = "0000000000"
            data["contactTACDenial"] = "0000000000"
            data["contactTACOnline"] = "DC4004F800"
            data["defaultDDOL"] = "9F3704"
            data["AcquirerIdentifier"] = "112233" // 9f01
            data["defaultTDOL"] = "9F0206"
            data["ThresholdValue"] = "000000002000"
            data["TargetPercentage"] = "00"
            data["MaxTargetPercentage"] = "00"
            data["AppSelIndicator"] = "00" //default 00 -part match 01 -full match
            data["TerminalAppPriority"] = "00" //TerminalCapabilities
            data["TerminalCapabilities"] = "E0C0B0"
            data["terminalCountryCode"] = "0360"
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)
        }

        fun addEMV_AID_Paramters() {
            val data = Hashtable<String, String>()
            data["CardType"] = "IcCard"
            data["aid"] = "A0000000041010"
            data["appVersion"] = "0002"
            data["terminalFloorLimit"] = "00000000"
            //data.put("terminalFloorLimit", Funs.DecNumStrToHexNumStr("000000010000"));
            //data.put("contactTACDefault", "D84000A800");
            data["contactTACDefault"] = "0000000000"
            data["contactTACDenial"] = "0000000000"
            data["contactTACOnline"] = "DC4004F800"
            data["defaultDDOL"] = "9F3704"
            data["AcquirerIdentifier"] = "112233" // 9f01
            data["defaultTDOL"] = "9F0206"
            data["ThresholdValue"] = "000000002000"
            data["TargetPercentage"] = "00"
            data["MaxTargetPercentage"] = "00"
            data["AppSelIndicator"] = "00" //default 00 -part match 01 -full match
            data["TerminalAppPriority"] = "00" //TerminalCapabilities
            data["TerminalCapabilities"] = "E0F8C8"

            data["terminalCountryCode"] = "0356"

            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data) //master

            data["aid"] = "A0000000043060" //Maestro
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A00000002501" //amex
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalFloorLimit"] = "000003E0"
            data["aid"] = "A0000000651010" //jcb
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalFloorLimit"] = "00000000"
            data["aid"] = "A0000000046000" //Cirrus
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000001523010" // Diners Club/Discover
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000006581010" // Mir
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0860001000001" //humo card
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000001523010" // Diners Club/Discover
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["contactTACDenial"] = "0010000000"
            data["aid"] = "A0000000031010" //visa
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000000032010"
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A0000005241010" //
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["aid"] = "A000000054480001" //TBD citizen
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0608"
            data["aid"] = "A0000006351010" //BancNet 菲律宾
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0682"
            data["aid"] = "A0000002281010" //Mada
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)

            data["terminalCountryCode"] = "0682"
            data["aid"] = "A0000002282010" //mada
            EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data)
        }

        fun init_NfcAid_CAPK() {
            var bret: Boolean


            val aidData = Hashtable<String, String>()
            //-------------------------------------------------
            //------------MasterCard----------------
            //-------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "MasterCard"
            aidData["ApplicationIdentifier"] = "A0000000041010" //9F06
            aidData["ApplicationVersionNumber"] = "0002" //9F09 or "ApplicationVersion"
            aidData["FloorLimit"] = "000000000000" //DF8123
            aidData["NoOnDeviceCVM"] = "999999999999" //contactless transaction limit DF8124
            aidData["OnDeviceCVM"] = "999999999999" // contactless transaction limit
            aidData["ReaderCVMRequiredLimit"] = "000000500000" //DF8126 or "CvmRequiredLimit"
            aidData["DefaultUDOL"] = "9F6A04" //DF811A
            aidData["TerminalActionCodesOnLine"] = "F45084800C" //DF8122 F45084800C
            aidData["TerminalActionCodesDenial"] = "0000000000" //DF8121
            aidData["TerminalActionCodesDefault"] = "F45084800C" //DF8120
            aidData["TerminalRiskManagement"] = "007A800000000000" //9F1D
            aidData["KernelConfiguration"] = "30" //  20 normal // 30 for RRP support
            aidData["CardDataInputCapability"] = "60" //DF8117  // 60
            aidData["CVMCapabilityPerCVMRequired"] = "60" //DF8118 //60 support online pin
            aidData["CVMCapabilityNoCVMRequired"] = "08" //DF8119
            aidData["MagStripeCVMCapabilityCVMRequired"] = "10" // DF811E = "10";
            aidData["SecurityCapability"] = "08" //DF811F
            aidData["MagStripeCVMCapabilityPerNoCVMRequired"] = "00" // DF812C = "00";
            //aidData.put("TerminalCountryCode", "0840");
            //aidData.put("IFDsn", "3030303030303030");
            bret = EmvNfcKernelApi.getInstance()
                .updateAID(ContantPara.Operation.ADD, aidData) //MasterCard


            Log.d("applog", "updateAID MasterCard:$bret")

            aidData["CVMCapabilityPerCVMRequired"] = "60"
            aidData["TerminalActionCodesOnLine"] = "F45004800C" //DF8122
            aidData["TerminalActionCodesDenial"] = "0000800000" //DF8121
            aidData["TerminalActionCodesDefault"] = "F45004800C" //DF8120
            aidData["TerminalRiskManagement"] = "4C7A800000000000" //9F1D
            aidData["ApplicationIdentifier"] = "A0000000043060" //9F06
            aidData["KernelConfiguration"] = "B0" //Maestro card not support MS mode

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "updateAID Maestro:$bret")


            //-------------------------------------------------------
            //--------------VISACARD-----------------------------
            // -------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "VisaCard"
            aidData["ApplicationIdentifier"] = "A0000000031010" //9F06
            aidData["TerminalTransactionQualifiers"] =
                "36004000" //9F66  //36004000    // 32204000 not support online PIN

            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F  //000000040000
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E   //000000030000

            aidData["LimitSwitch"] = "FE00" //9F92810A
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ProRestrictionDisable"] = "01"
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID1 $bret")

            aidData["ApplicationIdentifier"] = "A0000000032010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID2 $bret")
            aidData["ApplicationIdentifier"] = "A0000000033010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "VisaCard updateAID3 $bret")

            aidData["ApplicationIdentifier"] = "A0000006351010" //BancNet 菲律宾
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "Visa-BancNet updateAID4 $bret")


            //init_Visa_DRL();// need update it after default paramters


            //--------------------------------------------------------------
            //------------AMEXCARD--------------------------
            //------------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "AmexCard"
            aidData["ApplicationIdentifier"] = "A00000002501" //9F06
            aidData["TerminalTransactionQualifiers"] =
                "DCE00003" //9F6E  //58E00003  // D8E00003 support contact // Enhanced Contactless Reader Capabilities
            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000001200
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E
            aidData["LimitSwitch"] = "6800" //9F92810A
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ApplicationVersion"] = "0001" //9f09
            aidData["TerminalActionCodesOnLine"] = "DE00FC9800" //DF8122 //DE00FC9800
            aidData["TerminalActionCodesDenial"] = "0010000000" //DF8121 //0010000000
            aidData["TerminalActionCodesDefault"] = "DC50840000" //DF8120 //DC50FC9800


            /*
        ////////////////Dynamic Limit Set default//////////////////// if not support DRL set ,you need not set
        String defaultDRL="";
        String DRLset="";
        AmexDRL amexDRL=new AmexDRL();
        amexDRL.setCVMLimit("000000001000");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000001500");
        amexDRL.setIndex("00");
        amexDRL.setDefault(true);
        defaultDRL+=amexDRL.formTLVFormat();
        aidData.put("DefaultDRL", defaultDRL);
        Log.d("applog", "DefaultDRL:"+defaultDRL);
        ///////////Dynamic Limit Set 11////////////
        amexDRL.setCVMLimit("000000000200");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000000300");
        amexDRL.setIndex("0B");
        amexDRL.setDefault(false);
        DRLset+=amexDRL.formTLVFormat();
        ///////////Dynamic Limit Set 6////////////
        amexDRL.setCVMLimit("000000000200");
        amexDRL.setFloorLimit("000000000000");
        amexDRL.setTransLimit("000000000700");
        amexDRL.setIndex("06");
        amexDRL.setDefault(false);
        DRLset+=amexDRL.formTLVFormat();
        aidData.put("DRLSet", DRLset);
        Log.d("applog", "DRLSet:"+DRLset);
        ////////////////////////////Dynamic Limit Set ///////////////////
*/
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "AmexCard updateAID $bret")
            //----------------------------------------------------------
            //-----------------JCBCARD-------------------------------
            //-------------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "JcbCard"
            aidData["ApplicationIdentifier"] = "A0000000651010" //9F06
            aidData["ConfigurationCombinationOptions"] = "7B00" //
            aidData["StaticTerminalInterchangeProfile"] = "708000" //

            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000001200
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E

            aidData["EmvTerminalFloorLimit"] = "00004E20" //9F1B
            aidData["ApplicationVersion"] = "0200" //9f09
            aidData["TerminalActionCodesOnLine"] = "FC60ACF800" //DF8122
            aidData["TerminalActionCodesDenial"] = "0010000000" //DF8121
            aidData["TerminalActionCodesDefault"] = "FC6024A800" //DF8120

            aidData["ThresholdValue"] = "000000002000"
            aidData["TargetPercentage"] = "00"
            aidData["MaxTargetPercentage"] = "00"

            aidData["AcquirerIdentifier"] = "000000000010"
            //aidData.put("MerchantCategoryCode", "7032");
            //aidData.put("MerchantNameAndLocation", "5858204D45524348414E54205959204C4F434154494F4E");
            aidData["TerminalCapabilities"] = "E068C8"

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "JcbCard updateAID $bret")


            //-------------------------------------------------
            //--------------DISCOVERCARD-----------------------
            //-------------------------------------------------
            aidData.clear()
            aidData["CardType"] = "DiscoverCard"
            aidData["ApplicationIdentifier"] = "A000000152301002" //9F06
            aidData["TerminalTransactionQualifiers"] = "3600C000" // TTQ
            aidData["TransactionLimit"] = "999999999999" //9F92810D
            aidData["FloorLimit"] = "000000000000" //9F92810F   //000000000000
            aidData["CvmRequiredLimit"] = "000000500000" //9F92810E
            aidData["EmvTerminalFloorLimit"] = "00000000" //9F1B
            aidData["ApplicationVersion"] = "0001" //9f09
            aidData["TerminalActionCodesOnLine"] = "0000000000" //DF8122 FC60ACF800
            aidData["TerminalActionCodesDenial"] = "0000000000" //DF8121 0010000000
            aidData["TerminalActionCodesDefault"] = "0000000000" //DF8120 FC6024A800

            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "DiscoverCard updateAID $bret") //A0000003241010
            aidData["ApplicationIdentifier"] = "A0000001523010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)


            aidData["ApplicationIdentifier"] = "A0000003241010" //9F06
            bret = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData)
            Log.d("applog", "DinnerCard updateAID $bret")

        }

        fun emv_proc_onlinePin(isDUKPT: Boolean) {
            Log.i("applog", "emv_proc_onlinePin")

            val param: Bundle = Bundle()

            if (isDUKPT) param.putInt("PINKeyNo", 3)
            else param.putInt("PINKeyNo", 10)
            val cardno: String = "1122334455667788"


            Log.i("applog", "emv_proc_onlinePin cardno $cardno")
            param.putString("cardNo", cardno)
            param.putBoolean("sound", true)
            param.putInt("soundVolume", 1)
            param.putBoolean("onlinePin", true)
            param.putBoolean("FullScreen", true)
            param.putLong("timeOutMS", 30000)
            param.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12") // "4,4");   //
            param.putString("title", "Security PINPAD")
            param.putString(
                "message", "Please Enter PIN, \n $0.01"
            ) // use your real amount

/*            param.putBoolean("ShowLine", false)
            param.putShortArray("textSize", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("leftMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("topMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("rightMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putShortArray("bottomMargin", shortArrayOf(20, 30, 40, 50, 40, 30, 20))
            param.putStringArray(
                "numberText",
                arrayOf<String>(
                    "zero",
                    "one",
                    "two",
                    "three",
                    "four",
                    "five",
                    "six",
                    "seven",
                    "eight",
                    "nine"
                )
            )
            param.putIntArray(
                "backgroundColor",
                intArrayOf(
                    Color.BLUE,
                    Color.YELLOW,
                    Color.GREEN,
                    MaterialTheme.colorScheme.onSecondary,
                    Color.RED,
                    MaterialTheme.colorScheme.tertiary,
                    Color.LTGRAY
                )
            )
            param.putString("deleteText", "Delete1")
            param.putString("cancelText", "Cancel")
            param.putString("okText", "OK1")*/

            param.putBoolean("randomKeyboard", true)

            if (TextUtils.equals("I5000", Build.MODEL.uppercase(Locale.getDefault()))) {
                val transparentWhite = Color.TRANSPARENT

                val backgroundColor = intArrayOf(
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    transparentWhite,
                    Color.BLUE
                )
                val textColor = intArrayOf(
                    Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW,
                    Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW
                )
                param.putIntArray("backgroundColor", backgroundColor)
                param.putIntArray("textColor", textColor)

                //    textSize, margin
                //    each index(0-6):
                //    public static final int SECURITY_KEYBOARD_TITLE = 0;
                //    public static final int SECURITY_KEYBOARD_INFO = 1;
                //    public static final int SECURITY_KEYBOARD_PASSWORD = 2;
                //    public static final int SECURITY_KEYBOARD_KEY_NUMBER = 3;
                //    public static final int SECURITY_KEYBOARD_KEY_CANCEL = 4;
                //    public static final int SECURITY_KEYBOARD_KEY_DELETE = 5;
                //    public static final int SECURITY_KEYBOARD_KEY_OK = 6;
                val leftMargin = shortArrayOf(0, 0, 50, 0, 0, 0, 0)
                val rightMargin = shortArrayOf(0, 0, 50, 0, 0, 0, 0)
                val topMargin = shortArrayOf(0, 0, 10, 0, 0, 0, 0)
                val bottomMargin = shortArrayOf(0, 0, 10, 0, 0, 0, 0)
                param.putShortArray("leftMargin", leftMargin)
                param.putShortArray("rightMargin", rightMargin)
                param.putShortArray("topMargin", topMargin)
                param.putShortArray("bottomMargin", bottomMargin)
            }

            Log.i("applog", "getPinBlockEx ")

            if (isDUKPT)
                PinPadProviderImpl.getInstance().GetDukptPinBlock(param, this)
            else
                PinPadProviderImpl.getInstance().getPinBlockEx(param, this)

        }

        fun proc_offlinePin(pinEntryType: Int, isLastPinTry: Boolean, bundle: Bundle): Int {
            var iret = 0

            // TODO Auto-generated method stub
            val emvBundle = bundle


            Log.d(
                "applog",
                "proc_offlinePin pinEntryType = $pinEntryType isLastPinTry=$isLastPinTry"
            )

            val paramVar = Bundle()
            paramVar.putInt("inputType", 3) //Offline PlainPin
            paramVar.putInt("CardSlot", 0)

            paramVar.putBoolean("sound", true)
            paramVar.putInt("soundVolume", 1)
            paramVar.putBoolean("onlinePin", false)
            paramVar.putBoolean("FullScreen", true)
            paramVar.putLong("timeOutMS", 30000)
            paramVar.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12")
            paramVar.putString("title", "Security Keyboard")
            paramVar.putBoolean("randomKeyboard", true)
            val pinTryTimes = bundle.getInt("PinTryTimes")
            val isFirst = bundle.getBoolean("isFirstTime", false)
            Log.d("applog", "PinTryTimes:$pinTryTimes")
            if (isLastPinTry) {
                if (isFirst) paramVar.putString("message", "Please input PIN \nLast PIN Try")
                else paramVar.putString("message", "Please input PIN \nWrong PIN \nLast Pin Try")
            } else {
                if (isFirst) paramVar.putString("message", "Please input PIN \n")
                else {
                    paramVar.putString(
                        "message",
                        "Please input PIN \nWrong PIN \nPin Try Times:$pinTryTimes"
                    )
                }
            }


            if (pinEntryType == 1) {
                paramVar.putInt("inputType", 4) //Offline CipherPin

                val pub = emvBundle.getByteArray("pub")
                val publen = emvBundle.getIntArray("publen")
                val exp = emvBundle.getByteArray("exp")
                val explen = emvBundle.getIntArray("explen")

                Log.d("applog", "ModuleLen = " + publen!![0] + ": " + Funs.bytesToHexString(pub))
                Log.d("applog", "ExponentLen = " + explen!![0] + ": " + Funs.bytesToHexString(exp))


                val ModuleLen = publen!![0]
                val ExponentLen = explen!![0]
                val Module = ByteArray(ModuleLen)
                val Exponent = ByteArray(ExponentLen)

                if (ModuleLen == 0 || ExponentLen == 0) {
                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198)
                    return 0
                }

                System.arraycopy(pub, 0, Module, 0, ModuleLen)
                System.arraycopy(exp, 0, Exponent, 0, ExponentLen)

                paramVar.putInt("ModuleLen", ModuleLen) //Modulus length
                paramVar.putString("Module", Funs.bytesToHexString(Module)) //Module
                paramVar.putInt("ExponentLen", ExponentLen) //Exponent length
                paramVar.putString("Exponent", Funs.bytesToHexString(Exponent)) //Exponent
            }


            Log.d("applog", "proc_offlinePin getPinBlockEx start")

            /*
        paramVar.putInt("PinTryMode", 1);
        paramVar.putString("ErrorMessage", "Incorrect PIN, # More Retries");
        paramVar.putString("ErrorMessageLast", "Incorrect PIN, Last Chance");
        */
            val se = SEManager()
            iret = se.getPinBlockEx(paramVar, object : IInputActionListener.Stub() {
                override fun onInputChanged(type: Int, result: Int, bundle: Bundle) {
                    val resultBundle = bundle
                    try {
                        //    7101~7115 The number of remaining PIN tries(7101 PIN BLOCKED   7102 the last one chance  7103 two chances ....)
                        //		7006 PIN length error
                        //		7010 防穷举出错
                        //		7016 Wrong PIN
                        //		7071 The return code is wrong
                        //		7072 IC command failed
                        //		7073 Card data error
                        //		7074 PIN BLOCKED
                        //		7075 Encryption error
                        //
                        //The offline PIN verification result is sent to the kernel
                        //   use api EmvApi.sendOfflinePINVerifyResult();
                        //		    (-198)     //Return code error
                        //		    (-202)     //IC command failed
                        //		    (-192)     //PIN BLOCKED
                        //          (-199)     //user cancel or Pinpad timeout
                        //		    (1)        //bypass
                        //		    (0)        //success

                        Log.i(
                            "applog",
                            "proc_offlinePin：getPinBlockEx===onInputChanged：type=$type，result=$result"
                        )

                        if (type == 2) { // entering PIN
                        } else if (type == 0) //bypass
                        {
                            if (result == 0) {
                                Log.d("applog", "proc_offlinePin bypass")
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(1) //bypass
                            } else {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //return code error
                            }
                        } else if (type == 3) //Offline plaintext
                        {
                            Log.d("applog", "proc_offlinePin Plaintext offline")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(0) //Offline plaintext verify successfully
                            } else { //Incorrect PIN, try again
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                    } else {
                                        if ("7102" == arg1Str) {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt("PinTryTimes", 1)
                                            proc_offlinePin(
                                                pinEntryType,
                                                true,
                                                emvBundle
                                            ) //try again the last pin try
                                        } else {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt(
                                                "PinTryTimes",
                                                (arg1Str.substring(2, 4).toInt() - 1)
                                            )
                                            proc_offlinePin(
                                                pinEntryType,
                                                false,
                                                emvBundle
                                            ) //try again
                                        }
                                    }
                                } else if ("7074" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-202) //IC command failed
                                } else {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 4) //Offline encryption PIN
                        {
                            Log.d("applog", "proc_offlinePin Offline encryption")
                            if (result == 0) {
                                EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(0) //Offline encryption PIN verify successfully
                            } else {
                                val arg1Str = result.toString() + ""
                                if (arg1Str.length >= 4 && "71" == arg1Str.subSequence(0, 2)) {
                                    if ("7101" == arg1Str) {
                                        EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                    } else {
                                        Log.d(
                                            "applog",
                                            "proc_offlinePin Offline encryption entry pin again"
                                        )
                                        if ("7102" == arg1Str) {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt("PinTryTimes", 1)
                                            proc_offlinePin(
                                                pinEntryType,
                                                true,
                                                emvBundle
                                            ) //try again the last pin try
                                        } else {
                                            emvBundle.putBoolean("isFirstTime", false)
                                            emvBundle.putInt(
                                                "PinTryTimes",
                                                (arg1Str.substring(2, 4).toInt() - 1)
                                            )
                                            proc_offlinePin(
                                                pinEntryType,
                                                false,
                                                emvBundle
                                            ) //try again
                                        }
                                    }
                                } else if ("7074" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-192) //PIN BLOCKED
                                } else if ("7072" == arg1Str || "7073" == arg1Str) {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-202) //IC command failed(card removed)
                                } else {
                                    EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                                }
                            }
                        } else if (type == 0x10) // click Cancel button
                        {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-199) //cancel
                        } else if (type == 0x11) // pinpad timed out
                        {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-199) //timeout
                        } else {
                            EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198) //Return code error
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Log.d("applog", "proc_offlinePin exception")
                    }
                }
            })
            if (iret == -3 || iret == -4) EmvNfcKernelApi.getInstance().sendOfflinePINVerifyResult(-198)
            return iret
        }

        fun promptPin(
            pinpadBundle: Bundle?,
            isOnlinePin: Boolean,
            keyIndex: Int,
            plainKey: String?,
            randomLocation: Boolean
        ) {
            var pinpadBundle = pinpadBundle
            if (pinpadBundle == null || pinpadBundle.isEmpty) {
                pinpadBundle = Bundle()
                if (!isOnlinePin) {
                    pinpadBundle.putInt("inputType", 3) //Offline PlainPin
                    pinpadBundle.putInt("CardSlot", 0)
                }
                pinpadBundle.putString("cardNo", "1122334455667788")
                pinpadBundle.putBoolean("sound", true)
                pinpadBundle.putBoolean("bypass", false)
                pinpadBundle.putInt("soundVolume", 1)
                pinpadBundle.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12")
                pinpadBundle.putBoolean("onlinePin", isOnlinePin)
                pinpadBundle.putInt("PINKeyNo", keyIndex)
                pinpadBundle.putLong("timeOutMS", (30 * 1000).toLong())
                pinpadBundle.putBoolean("randomKeyboard", true)

                pinpadBundle.putBoolean("FullScreen", true)
                pinpadBundle.putInt("customKeyboardDialog", 5)

                pinpadBundle.putString("title", "Security Keyboard")
                pinpadBundle.putString("message", "Enter Your Pin")
            }
            try {
                PinPadProviderImpl.getInstance()
                    .getPinBlockEx(pinpadBundle, this)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        override fun onInput(p0: Int, p1: Int) {
            Log.d("EMV_LOG", "On Input: $p0, $p1")
        }

        override fun onConfirm(p0: ByteArray?, p1: Boolean) {
            if (p1) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.contentToString())
                    EmvNfcKernelApi.getInstance().sendPinEntry()
            }
        }

        override fun onConfirm_dukpt(p0: ByteArray?, p1: ByteArray?) {
            if (p0 == null) {
                EmvNfcKernelApi.getInstance().bypassPinEntry() //bypass
            } else {
                Log.d("EMV_APP", "PinBlock:" + p0.decodeToString())
                Log.d("EMV_APP", "KSN     :" + p1?.decodeToString())
                EmvNfcKernelApi.getInstance().sendPinEntry()
            }
        }

        override fun onCancel() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        override fun onTimeOut() {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }

        override fun onError(p0: Int) {
            EmvNfcKernelApi.getInstance().cancelPinEntry()
        }
    }
}