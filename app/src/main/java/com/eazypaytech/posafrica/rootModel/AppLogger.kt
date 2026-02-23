import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object AppLogger {

    private const val FILE_NAME = "eazy_logs.txt"
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5 MB

    fun log(context: Context, tag: String, message: String) {

        Log.d(tag, message)

        try {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, FILE_NAME)
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                file.delete()              // delete old file
                file.createNewFile()       // create fresh file
            }

            val writer = FileWriter(file, true)

            val timeStamp = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            writer.append("$timeStamp [$tag]: $message\n")
            writer.flush()
            writer.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}