package com.registro.alimentario

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RegistroApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            NotificationChannel(
                "meal_reminders",
                "Recordatorios de comida",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios opcionales para registrar tus comidas"
                manager.createNotificationChannel(this)
            }

            NotificationChannel(
                "clinical_comments",
                "Comentarios del equipo de salud",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando tu equipo de salud comenta en un registro"
                manager.createNotificationChannel(this)
            }

            NotificationChannel(
                "professional_notifications",
                "Actividad de pacientes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para profesionales sobre nuevos registros y comentarios de colegas"
                manager.createNotificationChannel(this)
            }
        }
    }
}
