package cash.atto.wallet.datasource

class AppDatabaseWeb : AppDatabase {
    override fun accountEntryDao() = AccountEntryDaoWeb()
    override fun workDao() = WorkDaoWeb()
}

class AccountEntryDaoWeb : AccountEntryDao {

    private val _accountEntries = HashMap<ByteArray, List<AccountEntryWeb>>()

    override suspend fun last(publicKey: ByteArray) = _accountEntries[publicKey]
        ?.lastOrNull()
        ?.toString()
        ?.encodeToByteArray()

    override suspend fun list(publicKey: ByteArray): List<String> = _accountEntries[publicKey]
        ?.map { it.toString() }
        ?: emptyList()

    suspend fun save(entry: AccountEntryWeb) {
        val currentValue = _accountEntries[entry.publicKey]
            ?.toMutableList()
            ?: emptyList()

        _accountEntries[entry.publicKey] = currentValue + entry
    }

    override suspend fun save(entry: AccountEntry) = save(entry as AccountEntryWeb)
}

class WorkDaoWeb : WorkDao {

    private var _work: WorkWeb? = null

    override suspend fun get() = _work
    suspend fun set(work: WorkWeb) { _work = work }
    override suspend fun set(work: Work) = set(work as WorkWeb)
    override suspend fun clear() { _work = null }
}

data class AccountEntryWeb(
    override val hash: ByteArray,
    override val publicKey: ByteArray,
    override val height: Long,
    override val entry: String
) : AccountEntry

data class WorkWeb(
    override val publicKey: ByteArray,
    override val value: ByteArray
) : Work

actual fun createAccountEntry(
    hash: ByteArray,
    publicKey: ByteArray,
    height: Long,
    entry: String
): AccountEntry = AccountEntryWeb(
    hash = hash,
    publicKey = publicKey,
    height = height,
    entry = entry
)


actual fun createWork(
    publicKey: ByteArray,
    value: ByteArray
): Work = WorkWeb(
    publicKey = publicKey,
    value = value
)