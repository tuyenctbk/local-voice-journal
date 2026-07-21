package com.localvoicejournal.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptographyHelperTest {

    @Test
    fun testBackupEncryptionDecryption() {
        val originalText = "{\"reflections\": [{\"transcript\": \"A beautiful silent park walk\", \"stressLevel\": \"LOW\"}]}"
        val password = "securePassphrase123".toCharArray()

        // Encrypt
        val encrypted = CryptographyHelper.encryptBackup(originalText, password)
        assertNotEquals(originalText, encrypted)
        assertTrue(encrypted.contains("::"))

        // Decrypt
        val decrypted = CryptographyHelper.decryptBackup(encrypted, password)
        assertEquals(originalText, decrypted)
    }

    @Test(expected = Exception::class)
    fun testBackupDecryptionWithWrongPassword() {
        val originalText = "Sensitive reflection content"
        val password = "correctPassword".toCharArray()
        val wrongPassword = "wrongPassword".toCharArray()

        val encrypted = CryptographyHelper.encryptBackup(originalText, password)
        // This should throw an Exception due to MAC tag mismatch or bad key
        CryptographyHelper.decryptBackup(encrypted, wrongPassword)
    }

    @Test
    fun testTranscriptEncryptionDecryptionFallback() {
        val plaintext = "Standard plain text reflection"
        val encrypted = CryptographyHelper.encryptTranscript(plaintext)
        assertEquals(plaintext, encrypted)

        val decrypted = CryptographyHelper.decryptTranscript(encrypted)
        assertEquals(plaintext, decrypted)
    }
}
