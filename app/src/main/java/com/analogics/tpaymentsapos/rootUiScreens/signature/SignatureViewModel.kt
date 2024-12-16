package com.analogics.tpaymentsapos.rootUiScreens.signature

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class SignatureViewModel @Inject constructor(private val dbRepository: TxnDBRepository) : ViewModel() {

    fun createSignatureBitmap(touchPoints: List<Offset>): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 4f
            strokeCap = android.graphics.Paint.Cap.ROUND // Set stroke cap
        }

        for (i in 1 until touchPoints.size) {
            val start = touchPoints[i - 1]
            val end = touchPoints[i]
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }

        return bitmap
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun onDoneButtonClick(navHostController: NavHostController, sharedViewModel: SharedViewModel, touchPoints: List<Offset>) {
        val bitmap = createSignatureBitmap(touchPoints)
        sharedViewModel.objRootAppPaymentDetail.signatureData = bitmapToBase64(bitmap)
        Log.d("SignatureViewModel", "Signature Data: ${sharedViewModel.objRootAppPaymentDetail.signatureData}")
    }
}
