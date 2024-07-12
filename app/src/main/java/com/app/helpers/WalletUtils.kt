package com.app.helpers

import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys

object WalletUtils {
    @Throws(Exception::class)
    fun generateEthereumWallet(): Credentials {
        val ecKeyPair = Keys.createEcKeyPair()
        return Credentials.create(ecKeyPair)
    }
}