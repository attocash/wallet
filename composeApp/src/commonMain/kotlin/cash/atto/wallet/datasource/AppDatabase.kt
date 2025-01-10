package cash.atto.wallet.datasource

interface AppDatabase {
    fun accountEntryDao(): AccountEntryDao
    fun workDao(): WorkDao
}

interface AccountEntryDao {
    suspend fun last(publicKey: ByteArray): ByteArray?
    suspend fun list(publicKey: ByteArray): List<String>
    suspend fun save(entry: AccountEntry)
}

interface WorkDao {
    suspend fun get(): Work?
    suspend fun set(work: Work)
    suspend fun clear()
}

interface AccountEntry {
    val hash: ByteArray
    val publicKey: ByteArray
    val height: Long
    val entry: String
}

interface Work {
    val publicKey: ByteArray
    val value: ByteArray
}

expect fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry

expect fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work