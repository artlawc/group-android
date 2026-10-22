package com.groupa.presentation.screen

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.groupa.coreui.R
import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeDeleteActionTest {

    @Test
    fun leftSwipeDeleteLabelIsCenteredInRevealedTrailingArea() {
        val action = leftSwipeDeleteAction

        assertEquals(R.string.action_confirm_delete, action.labelRes)
        assertEquals(Color.White, action.labelColor)
        assertEquals(Color(0xFFDC2626), action.backgroundColor)
        assertEquals(Alignment.Center, action.contentAlignment)
        assertEquals(Alignment.CenterEnd, action.areaAlignment)
    }
}
