package com.yigit033.matematikmacarasi.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.yigit033.matematikmacarasi.DailyTask
import com.yigit033.matematikmacarasi.MainActivity
import com.yigit033.matematikmacarasi.R
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Bildirim servis sınıfı.
 * Günlük görevler ve hatırlatıcılar için bildirim işlemlerini yönetir.
 */
class NotificationService private constructor(private val context: Context) {
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    
    companion object {
        // Bildirim kanalları
        const val CHANNEL_DAILY_TASKS = "daily_tasks_channel"
        const val CHANNEL_REMINDERS = "reminders_channel"
        
        // Bildirim ID'leri
        const val NOTIFICATION_ID_DAILY_TASKS = 1001
        const val NOTIFICATION_ID_REMINDER = 1002
        
        // Action tipleri
        const val ACTION_DAILY_TASK_REMINDER = "com.yigit033.matematikmacarasi.DAILY_TASK_REMINDER"
        const val ACTION_NEW_DAILY_TASKS = "com.yigit033.matematikmacarasi.NEW_DAILY_TASKS"
        
        // Tag
        private const val TAG = "NotificationService"
        
        @Volatile
        private var instance: NotificationService? = null
        
        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Bildirim kanallarını oluşturur (Android 8.0 ve üzeri)
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Günlük görevler kanalı
                val dailyTasksChannel = NotificationChannel(
                    CHANNEL_DAILY_TASKS,
                    "Günlük Görevler",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Yeni günlük görevler ve tamamlanmamış görevlerle ilgili bildirimler"
                    enableLights(true)
                    enableVibration(true)
                }
                
                // Hatırlatıcılar kanalı
                val remindersChannel = NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Hatırlatıcılar",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Önemli hatırlatıcılar ve uyarılar"
                    enableLights(true)
                    enableVibration(true)
                }
                
                // Kanalları NotificationManager'a kaydet
                notificationManager.createNotificationChannel(dailyTasksChannel)
                notificationManager.createNotificationChannel(remindersChannel)
                
                Log.d(TAG, "Bildirim kanalları oluşturuldu")
            } catch (e: Exception) {
                Log.e(TAG, "Bildirim kanalları oluşturulurken hata: ${e.message}", e)
            }
        }
    }
    
    /**
     * Günlük görev hatırlatıcısını zamanlar
     * @param hour Hatırlatma saati
     * @param minute Hatırlatma dakikası
     */
    fun scheduleTaskReminder(hour: Int, minute: Int) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                
                // Eğer belirlenen zaman geçmişse, bir sonraki güne ayarla
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = ACTION_DAILY_TASK_REMINDER
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_REMINDER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Alarm'ı ayarla (tekrarlanan, her gün)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            
            // Bir sonraki gün için de alarm ayarla
            val nextDayCalendar = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Tekrarlayan alarm için SharedPreferences kullan
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putInt("reminder_hour", hour)
                putInt("reminder_minute", minute)
                putBoolean("reminder_scheduled", true)
                apply()
            }
            
            Log.d(TAG, "Günlük görev hatırlatıcısı zamanlandı: ${calendar.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Hatırlatıcı zamanlanamadı: ${e.message}", e)
        }
    }
    
    /**
     * Hatırlatıcı bildirimini göster
     */
    fun showTaskReminderNotification() {
        try {
            // DailyTaskManager'dan tamamlanmamış görevleri al
            val dailyTaskManager = DailyTaskManager.getInstance(context)
            val tasks = dailyTaskManager.getTodayTasks()
            val uncompletedTasks = tasks.filter { !it.isCompleted }
            
            if (uncompletedTasks.isEmpty()) {
                Log.d(TAG, "Tamamlanmamış görev yok, bildirim gösterilmedi")
                return
            }
            
            // Bildirim içeriğini hazırla
            val title = "Günlük Görevler Seni Bekliyor!"
            val text = "${uncompletedTasks.size} tane tamamlanmamış görevin var"
            
            // Ana aktiviteye yönlendirecek intent
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_daily_tasks", true)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Bildirimi oluştur
            val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.daily_tasks_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                .build()
            
            // Bildirimi göster
            notificationManager.notify(NOTIFICATION_ID_REMINDER, notification)
            
            Log.d(TAG, "Görev hatırlatma bildirimi gösterildi")
        } catch (e: Exception) {
            Log.e(TAG, "Hatırlatma bildirimi gösterilemedi: ${e.message}", e)
        }
    }
    
    /**
     * Yeni günlük görevler bildirimi göster
     */
    fun showNewDailyTasksNotification() {
        try {
            val title = "Yeni Günlük Görevler!"
            val text = "Bugün için yeni görevler hazırlandı. Keşfetmek için tıkla!"
            
            // Ana aktiviteye yönlendirecek intent
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_daily_tasks", true)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Bildirimi oluştur
            val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_TASKS)
                .setSmallIcon(R.drawable.daily_tasks_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                .build()
            
            // Bildirimi göster
            notificationManager.notify(NOTIFICATION_ID_DAILY_TASKS, notification)
            
            Log.d(TAG, "Yeni günlük görevler bildirimi gösterildi")
        } catch (e: Exception) {
            Log.e(TAG, "Yeni görev bildirimi gösterilemedi: ${e.message}", e)
        }
    }
    
    /**
     * Gece yarısı için yeni görev bildirimi zamanla
     */
    fun scheduleNewDailyTasksNotification() {
        try {
            // Gece yarısı için takvim ayarla
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 5) // Gece yarısından 5 dakika sonra
                set(Calendar.SECOND, 0)
                
                // Eğer geçmiş zamanı gösteriyorsa bir sonraki güne ayarla
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = ACTION_NEW_DAILY_TASKS
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_DAILY_TASKS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Alarm'ı ayarla
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Yeni günlük görevler bildirimi zamanlandı: ${calendar.time}")
        } catch (e: Exception) {
            Log.e(TAG, "Yeni görev bildirimi zamanlanamadı: ${e.message}", e)
        }
    }
    
    /**
     * Tüm bildirim zamanlamalarını iptal et
     */
    fun cancelAllNotifications() {
        try {
            // Hatırlatıcıyı iptal et
            val reminderIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = ACTION_DAILY_TASK_REMINDER
            }
            
            val reminderPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_REMINDER,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(reminderPendingIntent)
            
            // Yeni görevler bildirimini iptal et
            val newTasksIntent = Intent(context, TaskReminderReceiver::class.java).apply {
                action = ACTION_NEW_DAILY_TASKS
            }
            
            val newTasksPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_DAILY_TASKS,
                newTasksIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(newTasksPendingIntent)
            
            // SharedPreferences'dan bilgiyi kaldır
            val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("reminder_scheduled", false).apply()
            
            Log.d(TAG, "Tüm bildirim zamanlamaları iptal edildi")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirimler iptal edilirken hata: ${e.message}", e)
        }
    }
    
    /**
     * Bildirim ayarlarını SharedPreferences'dan yükle
     */
    fun loadNotificationSettings() {
        try {
            val prefs = context.getSharedPreferences("matematik_macerasi_ayarlar", Context.MODE_PRIVATE)
            val isNotificationEnabled = prefs.getBoolean("notifications_enabled", true)
            
            if (isNotificationEnabled) {
                // Bildirim kanallarını yeniden oluştur
                createNotificationChannels()
                
                // Daha önce ayarlanmış bir hatırlatıcı varsa yeniden ayarla
                val notificationPrefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                val isReminderScheduled = notificationPrefs.getBoolean("reminder_scheduled", false)
                
                if (isReminderScheduled) {
                    val hour = notificationPrefs.getInt("reminder_hour", 19) // Varsayılan saat 19:00
                    val minute = notificationPrefs.getInt("reminder_minute", 0)
                    scheduleTaskReminder(hour, minute)
                    scheduleNewDailyTasksNotification()
                } else {
                    // Varsayılan hatırlatıcıyı ayarla (akşam 19:00)
                    scheduleTaskReminder(19, 0)
                    scheduleNewDailyTasksNotification()
                }
            } else {
                // Bildirimleri iptal et
                cancelAllNotifications()
            }
            
            Log.d(TAG, "Bildirim ayarları yüklendi: Bildirimler ${if(isNotificationEnabled) "aktif" else "pasif"}")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim ayarları yüklenirken hata: ${e.message}", e)
        }
    }
}

/**
 * Bildirim alarmlarını dinleyen BroadcastReceiver
 */
class TaskReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService = NotificationService.getInstance(context)
        
        when (intent.action) {
            NotificationService.ACTION_DAILY_TASK_REMINDER -> {
                // Hatırlatıcı bildirimi göster
                notificationService.showTaskReminderNotification()
                
                // Bir sonraki gün için yeniden zamanla
                val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
                val hour = prefs.getInt("reminder_hour", 19)
                val minute = prefs.getInt("reminder_minute", 0)
                notificationService.scheduleTaskReminder(hour, minute)
            }
            NotificationService.ACTION_NEW_DAILY_TASKS -> {
                // Yeni görevler bildirimi göster
                notificationService.showNewDailyTasksNotification()
                
                // Bir sonraki gün için yeniden zamanla
                notificationService.scheduleNewDailyTasksNotification()
            }
        }
    }
} 