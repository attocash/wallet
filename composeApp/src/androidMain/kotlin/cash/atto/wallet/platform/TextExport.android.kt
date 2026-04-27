package cash.atto.wallet.platform

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import org.koin.core.context.GlobalContext

actual suspend fun exportTextFile(
    fileName: String,
    mimeType: String,
    content: String,
): TextExportResult {
    val context = GlobalContext.get().get<android.content.Context>()
    val resolver = context.contentResolver
    val values =
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

    val uri =
        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create export file.")

    val outputStream = resolver.openOutputStream(uri) ?: error("Failed to open export stream.")
    outputStream.use { stream ->
        stream.write(content.encodeToByteArray())
    }

    return TextExportResult(location = "Downloads/$fileName")
}
