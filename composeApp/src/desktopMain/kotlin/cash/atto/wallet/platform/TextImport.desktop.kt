package cash.atto.wallet.platform

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual suspend fun importTextFile(
    @Suppress("UNUSED_PARAMETER") mimeTypes: List<String>,
    extensions: List<String>,
): TextImportResult? {
    val normalizedExtensions = extensions.map { it.removePrefix(".") }.filter { it.isNotBlank() }
    val chooser =
        JFileChooser().apply {
            dialogTitle = "Import Preferences"
            fileSelectionMode = JFileChooser.FILES_ONLY
            if (normalizedExtensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter("JSON files", *normalizedExtensions.toTypedArray())
            }
        }

    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
        return null
    }

    val file = chooser.selectedFile ?: return null

    return TextImportResult(
        location = file.absolutePath,
        content = File(file.absolutePath).readText(),
    )
}
