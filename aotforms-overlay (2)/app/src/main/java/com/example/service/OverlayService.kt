/*
 * Make by Kiều Lương Quân
 * This service manages the floating overlay windows (full screen FOV and draggable shoot controller).
 */

package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.OverlayConfig
import com.example.view.FovOverlayView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var fovOverlayView: FovOverlayView? = null
    private var controllerView: FrameLayout? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val CHANNEL_ID = "AotFormsOverlayChannel"
        private const val NOTIFICATION_ID = 4829
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceGracefully()
        showOverlay()

        return START_STICKY
    }

    private fun startForegroundServiceGracefully() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AotForms Aim Lock Active")
            .setContentText("Designed by Kiều Lương Quân")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Handle background service start exceptions gracefully
            e.printStackTrace()
        }
    }

    private fun showOverlay() {
        if (fovOverlayView != null) return // Already running

        // 1. Create full-screen FOV & ESP Overlay View
        fovOverlayView = FovOverlayView(this)
        val fovParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(fovOverlayView, fovParams)

        // 2. Create Floating Draggable TRIGGER/SHOOT Controller Bubble
        createDraggableController()

        OverlayConfig.isActivated = true
        OverlayConfig.triggerUpdate()
    }

    private fun createDraggableController() {
        controllerView = FrameLayout(this)
        
        // Circular button background
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(Color.parseColor("#FF3366"), Color.parseColor("#990033")) // Glowing red/pink gradient
            setStroke(3, Color.parseColor("#FFFFFF"))
        }

        // Parent layout container (glowing effect)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 12, 12, 12)
            background = bgDrawable
        }

        // Inner title text
        val titleText = TextView(this).apply {
            text = "AIM TRIGGER"
            setTextColor(Color.WHITE)
            textSize = 10f
            gravity = Gravity.CENTER
            setShadowLayer(5f, 0f, 0f, Color.RED)
            paint.isFakeBoldText = true
        }

        // Central Shoot Button
        val shootBtn = TextView(this).apply {
            text = "KÍCH HOẠT"
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            paint.isFakeBoldText = true
        }

        // Credit Text
        val creditText = TextView(this).apply {
            text = "by KQLQ"
            setTextColor(Color.parseColor("#00FF66"))
            textSize = 9f
            gravity = Gravity.CENTER
            paint.isFakeBoldText = true
        }

        container.addView(titleText)
        container.addView(shootBtn)
        container.addView(creditText)

        controllerView?.addView(container)

        // Layout Parameters for Draggable Window
        val btnSize = 180 // pixels size (circular bubble size)
        val controllerParams = WindowManager.LayoutParams(
            btnSize,
            btnSize,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 500
        }

        // Set touch listener for dragging
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = controllerParams.x
                    initialY = controllerParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    
                    // Update button press visual
                    bgDrawable.colors = intArrayOf(Color.parseColor("#00FF66"), Color.parseColor("#006633")) // Neon green on press
                    container.background = bgDrawable
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isMoving = true
                    }
                    
                    controllerParams.x = initialX + deltaX
                    controllerParams.y = initialY + deltaY
                    windowManager.updateViewLayout(controllerView, controllerParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Revert to original visual
                    bgDrawable.colors = intArrayOf(Color.parseColor("#FF3366"), Color.parseColor("#990033"))
                    container.background = bgDrawable

                    if (!isMoving) {
                        // Click detected! Toggle Auto-Aim Lock
                        OverlayConfig.isAimLocked = !OverlayConfig.isAimLocked
                        OverlayConfig.triggerUpdate()
                        
                        // Flash update message
                        shootBtn.text = if (OverlayConfig.isAimLocked) "LOCK ON" else "KÍCH HOẠT"
                        shootBtn.setTextColor(if (OverlayConfig.isAimLocked) Color.parseColor("#00FF66") else Color.WHITE)
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(controllerView, controllerParams)
    }

    private fun stopOverlay() {
        try {
            fovOverlayView?.let {
                windowManager.removeView(it)
                fovOverlayView = null
            }
            controllerView?.let {
                windowManager.removeView(it)
                controllerView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        OverlayConfig.isActivated = false
        OverlayConfig.isAimLocked = false
        OverlayConfig.triggerUpdate()
    }

    override fun onDestroy() {
        stopOverlay()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AotForms Channel"
            val descriptionText = "Manages background aim overlays"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
