package com.javanapps.moneymanager.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

/**
 * iOS-style "drum" wheel picker.
 *
 * Renders [itemCount] logical rows. When [infinite] is true the data is repeated many times and the
 * centered row is reported back modulo [itemCount], producing endless wrap-around scrolling. The
 * native [LazyColumn] fling + snap behaviour gives the same momentum/deceleration feel as iOS, while
 * a per-row 3D transform (scale + fade + perspective tilt) reproduces the curved drum look.
 *
 * The component is intentionally generic and theme-driven so it can be lifted into a standalone
 * Shamsi date/time picker library.
 *
 * @param itemCount number of distinct logical values (e.g. 12 months, 60 minutes).
 * @param initialIndex the logical index centered on first composition (0-based).
 * @param onSelectedIndexChange invoked with the logical index whenever the centered row settles.
 * @param infinite whether the wheel wraps around endlessly.
 * @param content renders the label for a logical index.
 */
@Composable
fun WheelPicker(
    itemCount: Int,
    initialIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    infinite: Boolean = true,
    visibleCount: Int = 5,
    itemHeight: Dp = 44.dp,
    enabledRange: IntRange = 0 until itemCount,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
    fadeColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable (index: Int) -> Unit,
) {
    require(itemCount > 0) { "itemCount must be > 0" }
    val odd = if (visibleCount % 2 == 0) visibleCount + 1 else visibleCount
    val half = odd / 2

    // The settle coroutines below are long-lived (keyed on listState, not on the range), so they must
    // read the *latest* range and callback rather than the ones captured at first composition — else a
    // wheel keeps clamping to a stale interval after a dependent column (year → month → day) widens it.
    val currentEnabled = rememberUpdatedState(enabledRange)
    val currentOnSelected = rememberUpdatedState(onSelectedIndexChange)

    // The nearest allowed logical value (the range is always a contiguous slice for date/time).
    fun clampToEnabled(logical: Int): Int {
        val range = currentEnabled.value
        return if (range.isEmpty()) logical else logical.coerceIn(range.first, range.last)
    }

    // Repeat the data so an "infinite" wheel has plenty of runway in both directions.
    val loops = if (infinite) 2_000 else 1
    val total = loops * itemCount
    val base = if (infinite) (loops / 2) * itemCount else 0
    val startRaw = (base + initialIndex).coerceIn(0, total - 1)

    // `half` blank rows pad each end so the first/last real row can sit dead-center. The data row
    // `r` therefore lives at list index `r + half`, and centering it is the exact, representable
    // position `initialFirstVisibleItemIndex = startRaw` (no fractional offset, no post-layout
    // correction — which is what made the old contentPadding + scrollToItem centering drift).
    val listCount = total + 2 * half
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startRaw)
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val haptics = LocalHapticFeedback.current

    fun rawToLogical(raw: Int) = if (infinite) ((raw % itemCount) + itemCount) % itemCount else raw

    // The logical value whose row sits closest to the vertical center, ignoring the blank pads.
    val centeredLogical by remember(listState, itemCount, infinite) {
        androidx.compose.runtime.derivedStateOf {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val nearest =
                info.visibleItemsInfo
                    .filter { it.index in half until half + total }
                    .minByOrNull { abs((it.offset + it.size / 2f) - viewportCenter) }
            if (nearest == null) initialIndex else rawToLogical(nearest.index - half)
        }
    }

    // Tick a subtle haptic as each row crosses the center, iOS-style.
    LaunchedEffect(listState, itemCount, infinite) {
        var first = true
        snapshotFlow { centeredLogical }
            .distinctUntilChanged()
            .collect {
                if (!first) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                first = false
            }
    }

    // Settle the wheel onto an allowed row and report it. If the centered row is disabled (the user
    // swiped into a blocked interval, or a dependent column's range just shrank), quickly animate to
    // the nearest enabled row — the iOS bounce-back — before committing.
    suspend fun settleOntoEnabled() {
        val current = centeredLogical
        val target = clampToEnabled(current)
        if (target != current) {
            val info = listState.layoutInfo
            val centerPx = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val centeredItem =
                info.visibleItemsInfo
                    .filter { it.index in half until half + total }
                    .minByOrNull { abs((it.offset + it.size / 2f) - centerPx) }
            if (centeredItem != null) {
                val currentRaw = centeredItem.index - half
                // currentRaw maps to `current`; shifting by the logical delta lands on a `target` row
                // adjacent to the current position, so the bounce is short.
                listState.animateScrollToItem((currentRaw + (target - current)).coerceIn(0, total - 1))
            }
        }
        currentOnSelected.value(target)
    }

    // Commit only once the wheel has come to rest, so a spinning wheel never mutates dependent state
    // (e.g. clamping the day to a shorter month) mid-fling.
    LaunchedEffect(listState, itemCount, infinite) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling -> if (!scrolling) settleOntoEnabled() }
    }

    // When the allowed interval changes (a dependent column updated) and we're already at rest on a
    // now-disabled row, bounce to the nearest allowed row right away.
    LaunchedEffect(enabledRange) {
        if (!listState.isScrollInProgress) settleOntoEnabled()
    }

    Box(
        modifier = modifier.height(itemHeight * odd).clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        // Stationary selection band that the rows slide across.
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(itemHeight)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp),
                    ),
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    // Soft top/bottom fade so rows dissolve into the background like a real drum.
                    .drawWithContent {
                        drawContent()
                        val fade = size.height * (half / odd.toFloat())
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    0f to fadeColor,
                                    (fade / size.height) to Color.Transparent,
                                    1f - (fade / size.height) to Color.Transparent,
                                    1f to fadeColor,
                                ),
                            blendMode = BlendMode.SrcOver,
                        )
                    },
        ) {
            items(listCount) { listIndex ->
                val raw = listIndex - half
                if (raw < 0 || raw >= total) {
                    Box(modifier = Modifier.height(itemHeight))
                } else {
                    val logical = rawToLogical(raw)
                    val isEnabled = enabledRange.contains(logical)
                    val selected = isEnabled && logical == centeredLogical
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .wheelTransform(listState, listIndex, half),
                        contentAlignment = Alignment.Center,
                    ) {
                        ProvideTextStyle(
                            textStyle.copy(
                                color =
                                    when {
                                        !isEnabled -> disabledColor
                                        selected -> selectedColor
                                        else -> unselectedColor
                                    },
                                textAlign = TextAlign.Center,
                            ),
                        ) {
                            content(logical)
                        }
                    }
                }
            }
        }
    }
}

/** A convenience for wheels whose labels are plain strings. */
@Composable
fun WheelPicker(
    itemCount: Int,
    initialIndex: Int,
    label: (Int) -> String,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    infinite: Boolean = true,
    visibleCount: Int = 5,
    itemHeight: Dp = 44.dp,
    enabledRange: IntRange = 0 until itemCount,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    WheelPicker(
        itemCount = itemCount,
        initialIndex = initialIndex,
        onSelectedIndexChange = onSelectedIndexChange,
        modifier = modifier,
        infinite = infinite,
        visibleCount = visibleCount,
        itemHeight = itemHeight,
        enabledRange = enabledRange,
        textStyle = textStyle,
    ) { index ->
        Text(text = label(index), maxLines = 1)
    }
}

/**
 * Scales, fades and tilts a row based on its live distance from the viewport center. Reading the
 * layout info inside [graphicsLayer] keeps the work in the draw phase, so it stays smooth every frame
 * without triggering recomposition.
 */
private fun Modifier.wheelTransform(
    listState: LazyListState,
    index: Int,
    half: Int,
): Modifier =
    graphicsLayer {
        val info = listState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return@graphicsLayer
        val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
        val itemCenter = item.offset + item.size / 2f
        // Distance from center expressed in row units (0 = centered, 1 = one row away).
        val rows = ((itemCenter - viewportCenter) / item.size).coerceIn(-half.toFloat(), half.toFloat())
        val t = (abs(rows) / half).coerceIn(0f, 1f)

        alpha = lerp(1f, 0.1f, t)
        val scale = lerp(1f, 0.72f, t)
        scaleX = scale
        scaleY = scale
        rotationX = -rows * 26f
        cameraDistance = 10f * density
    }

/** Where the wheels sit inside a dialog/sheet so fades blend correctly. */
val WheelPickerSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

/**
 * Polished dialog chrome shared by the date and time pickers: a rounded elevated card with a title,
 * an optional header (e.g. a style switcher or a live preview), the picker body, and cancel/confirm
 * actions.
 */
@Composable
internal fun PickerDialogScaffold(
    title: String,
    confirmText: String,
    cancelText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    body: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCancel,
        properties =
            androidx.compose.ui.window
                .DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                modifier
                    .padding(24.dp)
                    .width(340.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (header != null) {
                    Box(modifier = Modifier.padding(top = 16.dp)) { header() }
                }
                Box(modifier = Modifier.padding(vertical = 16.dp)) { body() }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(cancelText) }
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text(confirmText) }
                }
            }
        }
    }
}
