package com.app.helpers

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyStoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "my_secure_key"

    fun encryptPrivateKey(context: Context, privateKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getKey(context))
        return cipher.doFinal(privateKey)
    }

    fun decryptPrivateKey(context: Context, encryptedPrivateKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getKey(context))
        return cipher.doFinal(encryptedPrivateKey)
    }

    private fun getKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey(context)
        }

        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    private fun generateKey(context: Context) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setUserAuthenticationRequired(false) // Change to true if you need biometric authentication
            build()
        }
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun storeEncryptedPrivateKey(context: Context, encryptedPrivateKey: ByteArray) {
        val sharedPreferences = context.getSharedPreferences("my_secure_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("encrypted_private_key", encryptedPrivateKey.toString()).apply()
    }

    fun retrieveEncryptedPrivateKey(context: Context): ByteArray? {
        val sharedPreferences = context.getSharedPreferences("my_secure_prefs", Context.MODE_PRIVATE)
        val encryptedKeyFromPrefs = sharedPreferences.getString("encrypted_private_key", null)
        return encryptedKeyFromPrefs?.toByteArray()
    }
}