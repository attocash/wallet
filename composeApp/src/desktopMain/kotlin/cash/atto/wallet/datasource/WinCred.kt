package cash.atto.wallet.datasource

import cash.atto.wallet.datasource.CredAdvapi32.Companion.INSTANCE
import cash.atto.wallet.datasource.CredAdvapi32.PCREDENTIAL
import com.sun.jna.LastErrorException
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class WinCred : CredAdvapi32 {

    fun getCredential(target: String?): String {
        val pcredMem = PCREDENTIAL()

        try {
            if (CredRead(target, 1, 0, pcredMem)) {
                val credMem: CredAdvapi32.CREDENTIAL = CredAdvapi32.CREDENTIAL(pcredMem.credential)
                val passwordBytes: ByteArray =
                    credMem.CredentialBlob!!.getByteArray(0, credMem.CredentialBlobSize)

                val password = String(passwordBytes, Charset.forName("UTF-16LE"))
                return password
            } else {
                val err: Int = Native.getLastError()
                throw LastErrorException(err)
            }
        } finally {
            CredFree(pcredMem.credential)
        }
    }

    @Throws(UnsupportedEncodingException::class)
    fun setCredential(target: String, userName: String, password: String): Boolean {
        val credMem: CredAdvapi32.CREDENTIAL = CredAdvapi32.CREDENTIAL()

        credMem.Flags = 0
        credMem.TargetName = target
        credMem.Type = CredAdvapi32.CRED_TYPE_GENERIC
        credMem.UserName = userName
        credMem.AttributeCount = 0
        credMem.Persist = CredAdvapi32.CRED_PERSIST_ENTERPRISE
        val bpassword = password.toByteArray(charset("UTF-16LE"))
        credMem.CredentialBlobSize = bpassword.size
        credMem.CredentialBlob = getPointer(bpassword)
        if (!CredWrite(credMem, 0)) {
            val err: Int = Native.getLastError()
            throw LastErrorException(err)
        } else {
            return true
        }
    }

    @Throws(UnsupportedEncodingException::class)
    fun deleteCredential(target: String?): Boolean {
        if (!CredDelete(target, CredAdvapi32.CRED_TYPE_GENERIC, 0)) {
            val err: Int = Native.getLastError()
            throw LastErrorException(err)
        } else {
            return true
        }
    }

    @Throws(LastErrorException::class)
    override fun CredRead(
        targetName: String?,
        type: Int,
        flags: Int,
        pcredential: PCREDENTIAL?
    ): Boolean {
        synchronized(INSTANCE) {
            return INSTANCE.CredRead(targetName, type, flags, pcredential)
        }
    }

    @Throws(LastErrorException::class)
    override fun CredWrite(credential: CredAdvapi32.CREDENTIAL?, flags: Int): Boolean {
        synchronized(INSTANCE) {
            return INSTANCE.CredWrite(credential, flags)
        }
    }

    @Throws(LastErrorException::class)
    override fun CredDelete(targetName: String?, type: Int, flags: Int): Boolean {
        synchronized(INSTANCE) {
            return INSTANCE.CredDelete(targetName, type, flags)
        }
    }

    @Throws(LastErrorException::class)
    override fun CredFree(credential: Pointer?) {
        synchronized(INSTANCE) {
            INSTANCE.CredFree(credential)
        }
    }

    companion object {
        private fun getPointer(array: ByteArray): Pointer {
            val p: Pointer = Memory(array.size.toLong())
            p.write(0, array, 0, array.size)

            return p
        }
    }
}