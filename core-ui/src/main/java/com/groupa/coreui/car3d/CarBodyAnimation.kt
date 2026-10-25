package com.groupa.coreui.car3d

internal data class CarBodyAnimationSpec(
    val doorLeftRotationZ: Float,
    val doorRightRotationZ: Float,
    val doorLeftHingePivotX: Float,
    val doorRightHingePivotX: Float,
    val doorHingePivotY: Float,
    val hoodRotationX: Float,
    val hatchRotationX: Float,
    val hoodHingePivotY: Float,
    val hoodHingePivotZ: Float,
    val hatchHingePivotY: Float,
    val hatchHingePivotZ: Float,
    val doorLeftTranslationX: Float,
    val doorRightTranslationX: Float,
    val hoodTranslationZ: Float,
    val hatchTranslationZ: Float,
)

internal fun carBodyAnimationSpec(state: CarFrameState): CarBodyAnimationSpec =
    CarBodyAnimationSpec(
        doorLeftRotationZ = -60f * state.doorLeftProgress,
        doorRightRotationZ = 60f * state.doorRightProgress,
        doorLeftHingePivotX = LeftDoorHingePivotX,
        doorRightHingePivotX = RightDoorHingePivotX,
        doorHingePivotY = DoorHingePivotY,
        hoodRotationX = -55f * state.hoodProgress,
        hatchRotationX = 45f * state.hatchProgress,
        hoodHingePivotY = HoodHingePivotY,
        hoodHingePivotZ = HoodHingePivotZ,
        hatchHingePivotY = HatchHingePivotY,
        hatchHingePivotZ = HatchHingePivotZ,
        doorLeftTranslationX = 0.90f * state.explodeProgress,
        doorRightTranslationX = -0.90f * state.explodeProgress,
        hoodTranslationZ = 0.44f * state.explodeProgress,
        hatchTranslationZ = 0.30f * state.explodeProgress,
    )

private const val LeftDoorHingePivotX = 1.0871664f
private const val RightDoorHingePivotX = -1.0871131f
private const val DoorHingePivotY = -0.9923451f
private const val HoodHingePivotY = -1.1716428f
private const val HoodHingePivotZ = 0.8758436f
private const val HatchHingePivotY = 0.4667410f
private const val HatchHingePivotZ = 1.1309963f
