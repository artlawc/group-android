package com.groupa.coreui.car3d

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.abs

class CarBodyAnimationTest {
    @Test
    fun `doors swing in opposite outward directions`() {
        val spec = carBodyAnimationSpec(
            CarFrameState(
                turntableEnabled = false,
                doorLeftProgress = 1f,
                doorRightProgress = 1f,
                hoodProgress = 0f,
                hatchProgress = 0f,
                explodeProgress = 0f,
                spinWheels = false,
                driveLoop = false,
            ),
        )

        assertEquals(-60f, spec.doorLeftRotationZ)
        assertEquals(60f, spec.doorRightRotationZ)
        assertEquals(1.0871664f, spec.doorLeftHingePivotX)
        assertEquals(-1.0871131f, spec.doorRightHingePivotX)
        assertEquals(-0.9923451f, spec.doorHingePivotY)
    }

    @Test
    fun `hood and hatch rotate upward from upper hinges`() {
        val spec = carBodyAnimationSpec(
            CarFrameState(
                turntableEnabled = false,
                doorLeftProgress = 0f,
                doorRightProgress = 0f,
                hoodProgress = 1f,
                hatchProgress = 1f,
                explodeProgress = 0f,
                spinWheels = false,
                driveLoop = false,
            ),
        )

        assertEquals(-55f, spec.hoodRotationX)
        assertEquals(45f, spec.hatchRotationX)
        assertEquals(-1.1716428f, spec.hoodHingePivotY)
        assertEquals(0.8758436f, spec.hoodHingePivotZ)
        assertEquals(0.4667410f, spec.hatchHingePivotY)
        assertEquals(1.1309963f, spec.hatchHingePivotZ)
    }

    @Test
    fun `explode separates panels without forcing hinge rotations`() {
        val spec = carBodyAnimationSpec(
            CarFrameState(
                turntableEnabled = false,
                doorLeftProgress = 0f,
                doorRightProgress = 0f,
                hoodProgress = 0f,
                hatchProgress = 0f,
                explodeProgress = 1f,
                spinWheels = false,
                driveLoop = false,
            ),
        )

        assertEquals(0.90f, spec.doorLeftTranslationX)
        assertEquals(-0.90f, spec.doorRightTranslationX)
        assertEquals(0.44f, spec.hoodTranslationZ)
        assertEquals(0.30f, spec.hatchTranslationZ)
        assertEquals(0f, abs(spec.doorLeftRotationZ))
        assertEquals(0f, abs(spec.doorRightRotationZ))
        assertEquals(0f, abs(spec.hoodRotationX))
        assertEquals(0f, abs(spec.hatchRotationX))
    }
}
