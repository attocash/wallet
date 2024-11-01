package cash.atto.wallet.datasource

import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.hashString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UnsafePasswordDataSource : PasswordDataSourceDesktopImpl {
    private val passwordFile = File("Password.txt")

    private val seedToPasswordSeparator = "|"

    init {
        if (!passwordFile.exists())
            passwordFile.createNewFile()
    }

    @OptIn(InternalDecomposeApi::class)
    override suspend fun getPassword(seed: String): String? {
        if (passwordFile.length() == 0L)
            return null

        passwordFile.inputStream()
            .bufferedReader()
            .lineSequence()
            .forEach { line ->
                val (keyString, valueString) = line.split(seedToPasswordSeparator, limit = 2)

                if (keyString == seed.hashString())
                    return valueString
            }

        return null
    }

    @OptIn(InternalDecomposeApi::class)
    override suspend fun setPassword(
        seed: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        var keyFound = false

        var newText = passwordFile.inputStream()
            .bufferedReader()
            .lineSequence()
            .map { line ->
                val (keyString, _) = line.split(seedToPasswordSeparator, limit = 2)
                if (keyString == seed.hashString()) {
                    keyFound = true
                    return@map "$keyString$password"
                } else return@map line
            }

        if (!keyFound)
            newText += "${seed.hashString()}$password"

        passwordFile.delete()
        passwordFile.createNewFile()
        val writer = passwordFile.outputStream()
            .bufferedWriter()

        newText.forEach {
            writer.write(it)
            writer.newLine()
        }
    }
}