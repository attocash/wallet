package cash.atto.wallet.ui

import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AttoPaymentRequestsTest {
    private val address = AttoPublicKey(ByteArray(32) { it.toByte() }).toAddress(AttoAlgorithm.V1).value

    @Test
    fun `pasting the share URL restores the address and the requested amount`() {
        val request = AttoPaymentRequests.buildFromAtto(address, "1.25")
        val link = assertNotNull(AttoPaymentRequests.buildWalletDeepLinkFromPaymentRequest(request))

        assertEquals(AttoPaymentRequest(address, "1250000000"), AttoPaymentRequests.parse(link))
    }

    @Test
    fun `a share URL without an amount restores the address alone`() {
        val link = AttoPaymentRequests.buildWalletDeepLink(address, null)

        assertEquals(AttoPaymentRequest(address, null), AttoPaymentRequests.parse(link))
    }

    @Test
    fun `direct QR requests and bare addresses retain their existing spelling`() {
        val bare = address.removePrefix("atto://")

        assertEquals(AttoPaymentRequest(address, "42"), AttoPaymentRequests.parse("$address?amount=42"))
        assertEquals(AttoPaymentRequest(bare, "42"), AttoPaymentRequests.parse(AttoPaymentRequests.buildWalletDeepLink(bare, "42")))
    }

    @Test
    fun `a truncated escape stays visible to address validation`() {
        val incomplete = "atto%3A%2"

        assertEquals(
            AttoPaymentRequest(incomplete, null),
            AttoPaymentRequests.parse("https://wallet.atto.cash?receiverAddress=$incomplete"),
        )
    }
}
