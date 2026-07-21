package com.buylan.cryst.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

fun Modifier.swipeToSelect(
    onSwipe: () -> Unit,
    applyOffset: Boolean = false,
    onOffset: (Float) -> Unit = {},
    enabled: Boolean = true,
    horizontalSlop: Float = 20f,
    startMoveThreshold: Float = 12f,
    swipeThreshold: Float = 60f,
    maxOffset: Float = 120f,
    hapticThreshold: Float = 115f
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val offsetAnim = remember { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    val gestureModifier = if (enabled) {
        this.pointerInput(true) {
            awaitEachGesture {
                awaitFirstDown()
                var dx = 0f
                var dy = 0f
                var hapticTriggered = false
                var started = false
                var lastEvent: PointerEvent?

                do {
                    val event = awaitPointerEvent()
                    lastEvent = event
                    val change = event.changes.firstOrNull() ?: continue
                    val delta = change.positionChange()

                    dx += delta.x
                    dy += delta.y

                    if (!started && abs(dy) > horizontalSlop && abs(dy) > abs(dx)) {
                        break
                    }

                    if (!started && abs(dx) > startMoveThreshold && abs(dx) > abs(dy)) {
                        started = true
                        if (change.positionChange() != Offset.Zero) change.consume()
                    } else if (started) {
                        if (change.positionChange() != Offset.Zero) change.consume()
                    }

                    if (started) {
                        val newOffset = (offsetAnim.value + delta.x).coerceIn(-maxOffset, maxOffset)
                        scope.launch { offsetAnim.snapTo(newOffset) }
                        onOffset(offsetAnim.value)

                        if (abs(newOffset) >= hapticThreshold && !hapticTriggered) {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            hapticTriggered = true
                        } else if (abs(newOffset) < hapticThreshold) {
                            hapticTriggered = false
                        }
                    }

                } while (lastEvent.changes.any { it.pressed })

                val isHorizontalSwipe = abs(dx) > swipeThreshold && abs(dx) > abs(dy) * 2
                if (isHorizontalSwipe) {
                    onSwipe()
                }

                scope.launch {
                    offsetAnim.animateTo(0f, spring())
                    onOffset(offsetAnim.value)
                }
            }
        }
    } else this

    if (applyOffset) {
        gestureModifier.offset {
            IntOffset(offsetAnim.value.roundToInt(), 0)
        }
    } else {
        gestureModifier
    }
}