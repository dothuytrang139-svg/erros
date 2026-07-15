/*
 * Make by Kiều Lương Quân
 * Shared configuration for the Free Fire AotForms Aim Lock tool.
 */

package com.example

import com.example.model.Entity

object OverlayConfig {
    var isActivated: Boolean = false
    var fovRadius: Float = 250f // FOV circle radius in pixels
    var targetBone: String = "HEAD" // "HEAD", "NECK", "SPINE", "HIP"
    var isSimulating: Boolean = true
    var isAimLocked: Boolean = false
    var simulatedEntity: Entity = Entity()
    const val CREATOR: String = "Kiều Lương Quân"
    
    // Notify change callback so the overlay view can redraw immediately
    var onConfigChanged: (() -> Unit)? = null
    
    fun triggerUpdate() {
        onConfigChanged?.invoke()
    }
}
