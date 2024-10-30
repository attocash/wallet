package cash.atto.wallet.datasource

class RepresentativeDataSource(
    private val appDatabase: AppDatabase
) : RepresentativeDao {
    override suspend fun getRepresentatives(wallet: String) =
        appDatabase.getDao().getRepresentatives(wallet)

    override suspend fun updateRepresentative(representative: Representative) =
        appDatabase.getDao().createRepresentative(representative)

    override suspend fun createRepresentative(representative: Representative) =
        appDatabase.getDao().createRepresentative(representative)
}