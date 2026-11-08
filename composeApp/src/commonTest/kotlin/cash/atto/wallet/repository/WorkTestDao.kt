package cash.atto.wallet.repository

import cash.atto.wallet.datasource.Work
import cash.atto.wallet.datasource.WorkDao

internal class WorkTestDao : WorkDao {
    var stored: Work? = null
    var afterGet: suspend () -> Unit = {}
    var afterSet: suspend () -> Unit = {}
    var beforeClear: suspend () -> Unit = {}
    var failGet = false

    override suspend fun get(publicKey: ByteArray): Work? {
        check(!failGet) { "Storage unavailable" }
        val result = stored?.takeIf { it.publicKey.contentEquals(publicKey) }
        afterGet()
        return result
    }

    override suspend fun set(work: Work) {
        stored = work
        afterSet()
    }

    override suspend fun clear(publicKey: ByteArray) {
        beforeClear()
        if (stored?.publicKey?.contentEquals(publicKey) == true) stored = null
    }
}
