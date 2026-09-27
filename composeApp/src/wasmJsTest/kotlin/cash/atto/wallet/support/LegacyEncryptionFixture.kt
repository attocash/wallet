@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.support

import cash.atto.commons.toUint8Array
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.khronos.webgl.Uint8Array
import kotlin.io.encoding.Base64
import kotlin.js.Promise

internal const val LEGACY_SALT = "0123456789abcdef0123456789abcdef"

internal suspend fun encryptLegacy(
    plaintext: String,
    password: String,
): String = encryptLegacyAsync(plaintext, password, LEGACY_SALT).await<JsString>().toString()

// Independent fixture reproducing the released, unversioned WebCrypto format.
private fun encryptLegacyAsync(
    plaintext: String,
    password: String,
    salt: String,
): Promise<JsString> =
    js(
        """
    (async () => {
        const bytes = new TextEncoder();
        const base = await crypto.subtle.importKey('raw', bytes.encode(password), 'PBKDF2', false, ['deriveBits']);
        const bits = await crypto.subtle.deriveBits({name:'PBKDF2',hash:'SHA-256',salt:bytes.encode(salt),iterations:10000},base,128);
        const key = await crypto.subtle.importKey('raw',bits,'AES-GCM',false,['encrypt']);
        const result = await crypto.subtle.encrypt({name:'AES-GCM',iv:bytes.encode(salt)},key,bytes.encode(plaintext));
        return btoa(Array.from(new Uint8Array(result), b => String.fromCharCode(b)).join(''));
    })()
    """,
    )

internal suspend fun decryptEnvelopeIndependently(
    record: String,
    password: String,
): String {
    val fields = Json.parseToJsonElement(record).jsonObject
    val key =
        if (fields.getValue("version").jsonPrimitive.int == 2) {
            HashWasm
                .argon2id(
                    argon2Options(
                        password,
                        Base64.decode(fields.getValue("salt").jsonPrimitive.content).toUint8Array(),
                        fields.getValue("iterations").jsonPrimitive.int,
                        fields.getValue("memoryKiB").jsonPrimitive.int,
                        fields.getValue("parallelism").jsonPrimitive.int,
                    ),
                ).await<Uint8Array>()
        } else {
            null
        }
    return decryptEnvelopeAsync(record, password, key).await<JsString>().toString()
}

@JsModule("hash-wasm")
private external object HashWasm : JsAny {
    fun argon2id(options: JsAny): Promise<Uint8Array>
}

private fun argon2Options(
    password: String,
    salt: Uint8Array,
    iterations: Int,
    memorySize: Int,
    parallelism: Int,
): JsAny = js("({password, salt, iterations, memorySize, parallelism, hashLength:32, outputType:'binary'})")

private fun decryptEnvelopeAsync(
    record: String,
    password: String,
    argonKey: Uint8Array?,
): Promise<JsString> =
    js(
        """
    (async () => {
        const e = JSON.parse(record);
        const decode = text => Uint8Array.from(atob(text), c => c.charCodeAt(0));
        const bytes = new TextEncoder();
        let bits = argonKey;
        if (!bits) {
            const base = await crypto.subtle.importKey('raw',bytes.encode(password),'PBKDF2',false,['deriveBits']);
            bits = await crypto.subtle.deriveBits({name:'PBKDF2',hash:'SHA-256',salt:decode(e.salt),iterations:e.iterations},base,256);
        }
        const key = await crypto.subtle.importKey('raw',bits,'AES-GCM',false,['decrypt']);
        const parameters = e.version === 2 ? [e.argonVersion,e.memoryKiB,e.iterations,e.parallelism].join(':') : e.iterations;
        const aad = 'cash.atto.wallet:'+e.version+':'+e.purpose+':'+e.kdf+':'+parameters+':AES-256-GCM';
        const result = await crypto.subtle.decrypt({name:'AES-GCM',iv:decode(e.iv),additionalData:bytes.encode(aad),tagLength:128},key,decode(e.ciphertext));
        return new TextDecoder().decode(result);
    })()
    """,
    )

internal suspend fun encryptPbkdf2Envelope(
    plaintext: String,
    password: String,
    purpose: String,
): String = encryptPbkdf2EnvelopeAsync(plaintext, password, purpose).await<JsString>().toString()

private fun encryptPbkdf2EnvelopeAsync(
    plaintext: String,
    password: String,
    purpose: String,
): Promise<JsString> =
    js(
        """
    (async () => {
        const bytes = new TextEncoder();
        const salt = crypto.getRandomValues(new Uint8Array(16));
        const iv = crypto.getRandomValues(new Uint8Array(12));
        const base = await crypto.subtle.importKey('raw',bytes.encode(password),'PBKDF2',false,['deriveBits']);
        const bits = await crypto.subtle.deriveBits({name:'PBKDF2',hash:'SHA-256',salt,iterations:600000},base,256);
        const key = await crypto.subtle.importKey('raw',bits,'AES-GCM',false,['encrypt']);
        const aad = 'cash.atto.wallet:1:'+purpose+':PBKDF2-HMAC-SHA256:600000:AES-256-GCM';
        const result = await crypto.subtle.encrypt({name:'AES-GCM',iv,additionalData:bytes.encode(aad),tagLength:128},key,bytes.encode(plaintext));
        const encode = value => btoa(Array.from(new Uint8Array(value), byte => String.fromCharCode(byte)).join(''));
        return JSON.stringify({version:1,purpose,kdf:'PBKDF2-HMAC-SHA256',iterations:600000,salt:encode(salt),iv:encode(iv),ciphertext:encode(result)});
    })()
    """,
    )

internal fun failStorageWritesTo(key: String): Unit =
    js(
        """
    {
        const original = Storage.prototype.setItem;
        window.__walletTestRestoreStorage = () => { Storage.prototype.setItem = original; };
        Storage.prototype.setItem = function(name, value) {
            if (name === key) throw new DOMException('Synthetic storage failure', 'QuotaExceededError');
            return original.call(this, name, value);
        };
    }
    """,
    )

internal fun restoreStorageWrites(): Unit =
    js(
        """
    {
        if (window.__walletTestRestoreStorage) {
            window.__walletTestRestoreStorage();
            delete window.__walletTestRestoreStorage;
        }
    }
    """,
    )
