package cash.atto.wallet.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class UnsafeSeedDataSource : SeedDataSourceDesktopImpl {
    private val seedFile = File("Seed.txt")

    init {
        if (!seedFile.exists())
            seedFile.createNewFile()
    }

    override val seed = flow {
        if (seedFile.length() == 0L) {
            emit(null)
        }

        seedFile.inputStream()
            .bufferedReader()
            .lineSequence()
            .forEach { line ->
                emit(line)
            }
    }.flowOn(Dispatchers.IO)

    override suspend fun setSeed(seed: String) = seedFile.writeText(seed)
    override suspend fun clearSeed() {
        seedFile.delete()
    }
}