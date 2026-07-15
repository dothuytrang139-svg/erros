/*
 * Make by Kiều Lương Quân
 * This class represents the Free Fire character skeleton values and status in the game.
 */

package com.example.model

data class Vector3(val x: Float, val y: Float, val z: Float) {
    override fun toString(): String = String.format("%.2f, %.2f, %.2f", x, y, z)
}

data class Bool3(val x: Boolean, val y: Boolean, val z: Boolean) {
    override fun toString(): String = "($x, $y, $z)"
}

data class Entity(
    var isDead: Boolean = false,
    var isKnown: Boolean = true,
    var isTeam: Bool3 = Bool3(false, false, false),
    var head: Vector3 = Vector3(0f, 0f, 0f),
    var neck: Vector3 = Vector3(0f, 0f, 0f),
    var leftWrist: Vector3 = Vector3(0f, 0f, 0f),
    var rightWrist: Vector3 = Vector3(0f, 0f, 0f),
    var spine: Vector3 = Vector3(0f, 0f, 0f),
    var root: Vector3 = Vector3(0f, 0f, 0f),
    var hip: Vector3 = Vector3(0f, 0f, 0f),
    var rightCalf: Vector3 = Vector3(0f, 0f, 0f),
    var leftCalf: Vector3 = Vector3(0f, 0f, 0f),
    var rightFoot: Vector3 = Vector3(0f, 0f, 0f),
    var leftFoot: Vector3 = Vector3(0f, 0f, 0f),
    var leftHand: Vector3 = Vector3(0f, 0f, 0f),
    var leftShoulder: Vector3 = Vector3(0f, 0f, 0f),
    var rightShoulder: Vector3 = Vector3(0f, 0f, 0f),
    var rightWristJoint: Vector3 = Vector3(0f, 0f, 0f),
    var leftWristJoint: Vector3 = Vector3(0f, 0f, 0f),
    var rightElbow: Vector3 = Vector3(0f, 0f, 0f),
    var leftElbow: Vector3 = Vector3(0f, 0f, 0f),
    var health: Short = 200,
    var address: Long = 0x7F41C000L,
    var isKnocked: Boolean = false,
    var name: String = "EnemyPlayer"
)
