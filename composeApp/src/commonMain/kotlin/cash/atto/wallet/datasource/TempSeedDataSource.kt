package cash.atto.wallet.datasource

/*
* An in-memory data source for temporal storage of the seed.
* It should be only used in web targets to store the seed from
* the moment it is created to the moment the password is created
*/
class TempSeedDataSource {
    var seed: String? = null
}