package com.example.temubarang.features.auth.model

import io.github.jan.supabase.auth.exception.AuthRestException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Jangan membocorkan raw HTTP body/token/password; ubah menjadi pesan aman dan ramah. */
object AuthErrorMapper {
    fun describe(error: Throwable): String {
        if (error is AuthRestException) {
            return when (error.errorCode?.value) {
                "invalid_credentials" -> "Email atau password salah. Silakan coba lagi."
                "email_not_confirmed" -> "Email belum diverifikasi. Periksa kotak masuk dan folder spam."
                "user_already_exists", "email_exists" -> "Email tersebut sudah digunakan. Coba masuk dengan akun yang ada."
                "weak_password" -> "Password terlalu lemah. Gunakan kombinasi yang lebih kuat."
                "over_request_rate_limit", "over_email_send_rate_limit" ->
                    "Terlalu banyak percobaan. Coba lagi beberapa saat."
                "signup_disabled" -> "Pendaftaran akun sedang dinonaktifkan di Supabase."
                "email_address_invalid", "validation_failed" -> "Alamat email atau data yang dimasukkan tidak valid."
                else -> "Autentikasi gagal. Periksa data akun lalu coba lagi."
            }
        }
        // Error jaringan bisa dibungkus exception lain oleh Ktor.
        var cause: Throwable? = error
        while (cause != null) {
            if (cause is UnknownHostException) return "Server tidak ditemukan. Periksa internet dan URL Supabase."
            if (cause is SocketTimeoutException) return "Koneksi ke server terlalu lama. Coba lagi."
            if (cause is IOException) return "Tidak dapat terhubung ke server. Periksa koneksi internet."
            cause = cause.cause
        }
        // Fallback tanpa menampilkan detail internal SDK atau kredensial.
        return "Terjadi kesalahan saat menghubungi Supabase. Periksa koneksi lalu coba lagi."
    }
}
