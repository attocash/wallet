package cash.atto.wallet.support

// Generated independently with Node 24 crypto.argon2Sync and createCipheriv, password Synthetic7!.
// Both records use parameters different from the application's encryption defaults.
internal val ARGON2_MNEMONIC = (List(23) { "abandon" } + "art").joinToString(" ")
internal const val ARGON2_PREFERENCES = """{"addresses":[{"value":"synthetic-address","label":"Savings"}]}"""

internal val ARGON2_MNEMONIC_RECORD =
    """
    {"version":2,"purpose":"seed","kdf":"Argon2id","argonVersion":19,"memoryKiB":32768,
    "iterations":2,"parallelism":2,"salt":"MDEyMzQ1Njc4OWFiY2RlZg==","iv":"BwcHBwcHBwcHBwcH",
    "ciphertext":"C/Qul1AeRjtKOGh9WWgY074bJHpfdCIUTHugh2i4WaBgHkt1SlrDMWg4EYLFwr9d9v6IwcemP32/cbEQULvQC0tseyj8oXoHzHplGq+H2PrJyIVSpdlxJt2ijH/ecyJFbtfd5NH2kbiClk2U1b5WJVWFWuQoifRU9Abz/8CeKdvgB0kZAanSzQxF9etm2IdMcun2Skn+SFRGSp645Dui1SHGRCVSQK0amswOJ9QkAiudqlXPUYGH0TWq/yAdE6N8ajoSf/aygjCtxfY="}
    """.trimIndent()

internal val ARGON2_PREFERENCES_RECORD =
    """
    {"version":2,"purpose":"preferences","kdf":"Argon2id","argonVersion":19,"memoryKiB":131072,
    "iterations":4,"parallelism":4,"salt":"ZmVkY2JhOTg3NjU0MzIxMA==","iv":"CAgICAgICAgICAgI",
    "ciphertext":"BQYfO35RFi5rbkU0f+A+bHzTauIY5iaLmQDB/bCCz0coxf7my7HXHBVEKPvoMMXqznBiUZLqdUumPGKYNS0MuSIRMIgGlTLQ5eJz4dY2hQ=="}
    """.trimIndent()
