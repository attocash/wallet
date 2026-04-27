package cash.atto.wallet.platform

import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
actual suspend fun importTextFile(
    mimeTypes: List<String>,
    extensions: List<String>,
): TextImportResult? {
    val accept = (extensions + mimeTypes).distinct().joinToString(",")
    val imported = promptImportTextFile(accept).await<JsAny?>() ?: return null

    return TextImportResult(
        location = importedLocation(imported),
        content = importedContent(imported),
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun promptImportTextFile(accept: String): Promise<JsAny?> =
    js(
        """
        new Promise((resolve, reject) => {
          const input = document.createElement('input');
          input.type = 'file';
          input.accept = accept;
          input.style.display = 'none';
          const cleanup = () => {
            if (input.parentNode) {
              input.parentNode.removeChild(input);
            }
          };
          input.addEventListener('change', async () => {
            const file = input.files && input.files[0];
            cleanup();
            if (!file) {
              resolve(null);
              return;
            }
            try {
              const content = await file.text();
              resolve({ location: file.name, content });
            } catch (error) {
              reject(error);
            }
          });
          input.addEventListener('cancel', () => {
            cleanup();
            resolve(null);
          });
          document.body.appendChild(input);
          input.click();
        })
        """,
    )

@OptIn(ExperimentalWasmJsInterop::class)
private fun importedLocation(imported: JsAny): String = js("imported.location")

@OptIn(ExperimentalWasmJsInterop::class)
private fun importedContent(imported: JsAny): String = js("imported.content")
