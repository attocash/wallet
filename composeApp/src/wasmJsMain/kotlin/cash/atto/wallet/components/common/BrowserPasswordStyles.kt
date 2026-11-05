@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import attowallet.composeapp.generated.resources.Res
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_placeholder
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLElement
import kotlin.js.JsAny
import kotlin.js.Promise

internal suspend fun preloadBrowserPasswordFont() {
    loadBrowserPasswordFont(Res.getUri("font/noto_sans_variable.ttf")).await<JsAny?>()
}

private fun loadBrowserPasswordFont(url: String): Promise<JsAny?> =
    js(
        """
        new FontFace('AttoPassword', 'url(' + JSON.stringify(url) + ')', { weight: '100 900' })
          .load()
          .then(font => { document.fonts.add(font); return null; })
        """,
    )

internal fun createBrowserPasswordStyles(): HTMLElement =
    (document.createElement("style") as HTMLElement).apply {
        textContent =
            """
            .atto-password-field, .atto-password-submit, .atto-password-terms {
              font-family: AttoPassword, sans-serif;
              box-sizing: border-box;
              color-scheme: dark;
            }
            .atto-password-field label {
              display: block;
              color: ${dark_text_secondary.css()};
              font-size: 13px;
              font-weight: 500;
              line-height: 16px;
              margin-bottom: 8px;
            }
            .atto-password-field label[hidden] { display: none; }
            .atto-password-control {
              display: flex;
              box-sizing: border-box;
              height: 56px;
              background: ${dark_surface.css()};
              border: 1px solid ${dark_border.css()};
              border-radius: 8px;
            }
            .atto-password-field[data-error=true] .atto-password-control { border-color: ${dark_danger.css()}; }
            .atto-password-control:focus-within { border-color: ${dark_accent.css()}; }
            .atto-password-control input {
              flex: 1;
              min-width: 0;
              width: 100%;
              padding: 0 16px;
              font: 400 14px AttoPassword, sans-serif;
              color: ${dark_text_primary.css()};
              caret-color: ${dark_accent.css()};
              background: transparent;
              border: 0;
              outline: 0;
              border-radius: 8px;
            }
            .atto-password-control input::placeholder { color: ${dark_placeholder.css()}; opacity: 1; }
            .atto-password-control input::-ms-reveal { display: none; }
            .atto-password-reveal {
              display: flex;
              align-items: center;
              justify-content: center;
              flex-shrink: 0;
              width: 48px;
              color: ${dark_text_muted.css()};
              background: transparent;
              border: 0;
              border-radius: 8px;
              cursor: pointer;
            }
            .atto-password-reveal:hover { color: ${dark_text_primary.css()}; }
            .atto-password-reveal:focus-visible { outline: 2px solid ${dark_accent.css()}; outline-offset: -4px; }
            .atto-password-terms {
              display: flex;
              align-items: flex-start;
              gap: 10px;
              color: ${dark_text_secondary.css()};
              font-size: 12px;
              line-height: 18px;
            }
            .atto-password-terms input {
              appearance: none;
              box-sizing: border-box;
              flex-shrink: 0;
              width: 18px;
              height: 18px;
              margin: 1px 0 0;
              border: 1px solid ${dark_border.css()};
              border-radius: 4px;
              cursor: pointer;
            }
            .atto-password-terms input:checked {
              background: ${dark_accent.css()};
              border-color: ${dark_accent.css()};
            }
            .atto-password-terms input:checked::after {
              content: '';
              display: block;
              width: 4px;
              height: 8px;
              margin: 2px 0 0 5px;
              border: solid ${dark_accent_on.css()};
              border-width: 0 2px 2px 0;
              transform: rotate(45deg);
            }
            .atto-password-terms label { cursor: pointer; }
            .atto-password-terms[hidden] { display: none; }
            .atto-password-terms button {
              padding: 0;
              border: 0;
              background: transparent;
              color: ${dark_accent.css()};
              font: inherit;
              cursor: pointer;
            }
            .atto-password-terms :focus-visible { outline: 1px solid ${dark_accent.css()}; outline-offset: -1px; }
            .atto-password-submit {
              padding: 0 20px;
              border: 0;
              border-radius: 12px;
              background: ${dark_accent.css()};
              color: ${dark_accent_on.css()};
              font-size: 15px;
              font-weight: 700;
              cursor: pointer;
            }
            .atto-password-submit:hover:enabled { filter: brightness(1.05); }
            .atto-password-submit:disabled { background: ${dark_accent.copy(alpha = 0.4f).css()}; cursor: default; }
            .atto-password-submit:focus-visible { outline: 2px solid ${dark_text_primary.css()}; outline-offset: -5px; }
            """.trimIndent()
    }

private fun Color.css(): String = "rgba(${(toArgb() shr 16) and 255}, ${(toArgb() shr 8) and 255}, ${toArgb() and 255}, $alpha)"
