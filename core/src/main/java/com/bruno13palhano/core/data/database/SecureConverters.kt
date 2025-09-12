package com.bruno13palhano.core.data.database

import android.util.Base64
import androidx.room.TypeConverter
import com.bruno13palhano.core.data.secure.EncryptedBytes
import com.bruno13palhano.core.data.secure.EncryptedString
import com.bruno13palhano.core.data.secure.KeyStoreManager
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

internal object SecureConverters {
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128

    private fun getSecretKey() = KeyStoreManager.getOrCreateSecretKey()

    @TypeConverter
    @JvmStatic
    fun fromEncryptedString(value: EncryptedString?): String? {
        val plain = value?.value ?: return null
        val cipher = Cipher.getInstance(AES_MODE)

        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv ?: ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    @TypeConverter
    @JvmStatic
    fun toEncryptedString(dbValue: String?): EncryptedString? {
        if (dbValue == null) return null
        val all = Base64.decode(dbValue, Base64.NO_WRAP)

        val iv = all.copyOfRange(0, IV_SIZE)
        val encrypted = all.copyOfRange(IV_SIZE, all.size)

        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decrypted = cipher.doFinal(encrypted)
        return EncryptedString(String(decrypted, Charsets.UTF_8))
    }

    @TypeConverter
    @JvmStatic
    fun fromEncryptedBytes(value: EncryptedBytes?): ByteArray? {
        val plain = value?.value ?: return null
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv ?: ByteArray(IV_SIZE).apply { SecureRandom().nextBytes(this) }
        val encrypted = cipher.doFinal(plain)
        return iv + encrypted
    }

    @TypeConverter
    @JvmStatic
    fun toEncryptedBytes(dbValue: ByteArray?): EncryptedBytes? {
        if (dbValue == null) return null

        val iv = dbValue.copyOfRange(0, IV_SIZE)
        val encrypted = dbValue.copyOfRange(IV_SIZE, dbValue.size)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decrypted = cipher.doFinal(encrypted)
        return EncryptedBytes(decrypted)
    }
}