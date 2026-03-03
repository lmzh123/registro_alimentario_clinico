package com.registro.alimentario.ui.patient

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.registro.alimentario.R
import com.registro.alimentario.model.TipoComida

private const val PREFS_NAME = "notification_prefs"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var remindersEnabled by remember {
        mutableStateOf(prefs.getBoolean("reminders_enabled", false))
    }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_cd))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.notification_settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Global reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notification_enable_label),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && notificationPermission?.status?.isGranted == false) {
                            notificationPermission.launchPermissionRequest()
                        } else {
                            remindersEnabled = enabled
                            prefs.edit().putBoolean("reminders_enabled", enabled).apply()
                            if (!enabled) cancelAllReminders(context)
                        }
                    }
                )
            }

            if (notificationPermission?.status?.shouldShowRationale == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.permission_notification_rationale),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (remindersEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Horarios de recordatorio",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                val mealReminders = listOf(
                    TipoComida.DESAYUNO to "08:00",
                    TipoComida.ALMUERZO to "13:00",
                    TipoComida.MERIENDA to "17:00",
                    TipoComida.CENA to "20:00"
                )
                mealReminders.forEach { (meal, defaultTime) ->
                    MealReminderRow(
                        meal = meal,
                        prefs = prefs,
                        defaultTime = defaultTime,
                        onSchedule = { hour, minute -> scheduleReminder(context, meal, hour, minute) },
                        onCancel = { cancelReminder(context, meal) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealReminderRow(
    meal: TipoComida,
    prefs: SharedPreferences,
    defaultTime: String,
    onSchedule: (Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    val key = "reminder_${meal.id}"
    var enabled by remember { mutableStateOf(prefs.getBoolean(key, false)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(meal.displayName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = prefs.getString("${key}_time", defaultTime) ?: defaultTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                enabled = checked
                prefs.edit().putBoolean(key, checked).apply()
                if (checked) {
                    val time = (prefs.getString("${key}_time", defaultTime) ?: defaultTime).split(":")
                    onSchedule(time[0].toInt(), time[1].toInt())
                } else {
                    onCancel()
                }
            }
        )
    }
}

private fun scheduleReminder(context: Context, meal: TipoComida, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent("com.registro.alimentario.MEAL_REMINDER").apply {
        putExtra("meal_id", meal.id)
        putExtra("meal_name", meal.displayName)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        meal.ordinal,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    alarmManager.setInexactRepeating(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

private fun cancelReminder(context: Context, meal: TipoComida) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent("com.registro.alimentario.MEAL_REMINDER")
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        meal.ordinal,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    pendingIntent?.let { alarmManager.cancel(it) }
}

private fun cancelAllReminders(context: Context) {
    TipoComida.entries.forEach { cancelReminder(context, it) }
}
