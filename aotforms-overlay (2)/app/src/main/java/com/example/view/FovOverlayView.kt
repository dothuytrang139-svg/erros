/*
 * Make by Kiều Lương Quân
 * This custom view draws the FOV aim lock circle and simulates/animates Free Fire skeleton bone vectors.
 */

package com.example.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.example.OverlayConfig
import com.example.model.Bool3
import com.example.model.Entity
import com.example.model.Vector3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FovOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val fovPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#00FF66") // Neon Green
    }

    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#00CCFF") // Neon Blue
    }

    private val bonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#FFFFFF") // White bones
    }

    private val jointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#00CCFF") // Blue joints
    }

    private val lockLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.parseColor("#FF3366") // Neon Red/Pink
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00FF66")
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF3366")
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // Joint list helper for drawing
    private val bonePath = Path()

    // Simulation variables
    private var lastTime = System.currentTimeMillis()
    private var angle = 0f

    init {
        // Redraw automatically
        OverlayConfig.onConfigChanged = {
            postInvalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        
        if (centerX <= 0 || centerY <= 0) return

        // 1. Draw FOV Circle
        val radius = OverlayConfig.fovRadius
        fovPaint.color = if (OverlayConfig.isAimLocked) Color.parseColor("#FF3366") else Color.parseColor("#00FF66")
        canvas.drawCircle(centerX, centerY, radius, fovPaint)

        // 2. Draw Crosshair (Tiny cross in center)
        canvas.drawLine(centerX - 15, centerY, centerX + 15, centerY, crosshairPaint)
        canvas.drawLine(centerX, centerY - 15, centerX, centerY + 15, crosshairPaint)
        canvas.drawCircle(centerX, centerY, 3f, crosshairPaint)

        // 3. Simulate Enemy Skeleton Movement
        val timeSec = System.currentTimeMillis() / 1000.0
        // Drift enemy position smoothly around the screen
        val enemyX = centerX + sin(timeSec * 0.9).toFloat() * (centerX * 0.6f)
        val enemyY = centerY + cos(timeSec * 0.7).toFloat() * (centerY * 0.4f)

        // Map positions to our Entity model
        val entity = OverlayConfig.simulatedEntity
        entity.name = "Target_KQLQ_FF"
        entity.health = 200
        entity.isDead = false
        entity.isKnocked = false
        entity.isTeam = Bool3(false, false, false)

        // Fill skeletal bone vectors (3D representation projected to 2D)
        entity.head = Vector3(enemyX, enemyY - 110f, 15f)
        entity.neck = Vector3(enemyX, enemyY - 80f, 15f)
        entity.spine = Vector3(enemyX, enemyY - 20f, 15f)
        entity.hip = Vector3(enemyX, enemyY + 30f, 15f)
        
        entity.leftShoulder = Vector3(enemyX - 45f, enemyY - 75f, 15f)
        entity.rightShoulder = Vector3(enemyX + 45f, enemyY - 75f, 15f)
        
        entity.leftElbow = Vector3(enemyX - 60f, enemyY - 40f, 15f)
        entity.rightElbow = Vector3(enemyX + 60f, enemyY - 40f, 15f)
        
        entity.leftWrist = Vector3(enemyX - 70f, enemyY - 10f, 15f)
        entity.rightWrist = Vector3(enemyX + 70f, enemyY - 10f, 15f)
        entity.leftHand = Vector3(enemyX - 75f, enemyY - 2f, 15f)
        
        entity.leftCalf = Vector3(enemyX - 30f, enemyY + 80f, 15f)
        entity.rightCalf = Vector3(enemyX + 30f, enemyY + 80f, 15f)
        
        entity.leftFoot = Vector3(enemyX - 35f, enemyY + 130f, 15f)
        entity.rightFoot = Vector3(enemyX + 35f, enemyY + 130f, 15f)

        // Trigger updates so MainActivity can see coords changing
        OverlayConfig.triggerUpdate()

        // 4. Draw Skeleton ESP
        if (OverlayConfig.isSimulating) {
            // Joints
            val joints = listOf(
                entity.head, entity.neck, entity.spine, entity.hip,
                entity.leftShoulder, entity.rightShoulder,
                entity.leftElbow, entity.rightElbow,
                entity.leftWrist, entity.rightWrist,
                entity.leftCalf, entity.rightCalf,
                entity.leftFoot, entity.rightFoot
            )

            // Draw bone segments
            bonePaint.color = Color.parseColor("#44FFFFFF") // Semi transparent bones
            
            // Spine & Body line
            drawBoneSegment(canvas, entity.head, entity.neck)
            drawBoneSegment(canvas, entity.neck, entity.spine)
            drawBoneSegment(canvas, entity.spine, entity.hip)
            
            // Left arm
            drawBoneSegment(canvas, entity.neck, entity.leftShoulder)
            drawBoneSegment(canvas, entity.leftShoulder, entity.leftElbow)
            drawBoneSegment(canvas, entity.leftElbow, entity.leftWrist)
            
            // Right arm
            drawBoneSegment(canvas, entity.neck, entity.rightShoulder)
            drawBoneSegment(canvas, entity.rightShoulder, entity.rightElbow)
            drawBoneSegment(canvas, entity.rightElbow, entity.rightWrist)
            
            // Left leg
            drawBoneSegment(canvas, entity.hip, entity.leftCalf)
            drawBoneSegment(canvas, entity.leftCalf, entity.leftFoot)
            
            // Right leg
            drawBoneSegment(canvas, entity.hip, entity.rightCalf)
            drawBoneSegment(canvas, entity.rightCalf, entity.rightFoot)

            // Draw joints
            for (j in joints) {
                canvas.drawCircle(j.x, j.y, 6f, jointPaint)
            }
            // Draw head circle specifically
            canvas.drawCircle(entity.head.x, entity.head.y, 16f, jointPaint)

            // 5. Auto Aim-Lock Logic
            // Get selected bone vector
            val targetVec = when (OverlayConfig.targetBone) {
                "HEAD" -> entity.head
                "NECK" -> entity.neck
                "SPINE" -> entity.spine
                "HIP" -> entity.hip
                else -> entity.head
            }

            // Calculate distance from screen center to the selected bone
            val dist = sqrt((targetVec.x - centerX) * (targetVec.x - centerX) + (targetVec.y - centerY) * (targetVec.y - centerY))
            val isInsideFov = dist <= radius

            if (isInsideFov) {
                // Enemy inside FOV! Let's lock
                lockLinePaint.color = Color.parseColor("#FF3366") // Bright Red Lock
                lockLinePaint.strokeWidth = if (OverlayConfig.isAimLocked) 7f else 4f
                
                // Draw connecting lock line from crosshair to target bone
                canvas.drawLine(centerX, centerY, targetVec.x, targetVec.y, lockLinePaint)
                
                // Draw locking cursor box around the target bone
                val boxSize = if (OverlayConfig.isAimLocked) 25f else 18f
                canvas.drawRect(
                    targetVec.x - boxSize, targetVec.y - boxSize,
                    targetVec.x + boxSize, targetVec.y + boxSize,
                    lockLinePaint
                )

                // Floating Lock Text
                textPaint.color = Color.parseColor("#FF3366")
                val statusText = if (OverlayConfig.isAimLocked) "TARGET LOCKED - HEADSHOT ENGAGED" else "TARGET ACQUIRED"
                canvas.drawText(statusText, centerX, centerY - radius - 30f, textPaint)
            } else {
                // Enemy outside FOV
                textPaint.color = Color.parseColor("#00FF66")
                canvas.drawText("SCANNING FOR ENEMY...", centerX, centerY - radius - 30f, textPaint)
            }
        }

        // 6. Draw branding watermark overlay
        canvas.drawText("AotForms • Code by Kiều Lương Quân", centerX, height - 80f, textPaint)

        // Loop animation at ~60fps
        postInvalidateOnAnimation()
    }

    private fun drawBoneSegment(canvas: Canvas, from: Vector3, to: Vector3) {
        canvas.drawLine(from.x, from.y, to.x, to.y, bonePaint)
    }
}
