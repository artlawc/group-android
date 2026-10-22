package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlin.math.roundToInt
import com.groupa.coreui.R
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.domain.model.Contact
import kotlinx.coroutines.launch

internal data class ContactSection(
    val initial: String,
    val contacts: List<Contact>,
)

internal fun filterContacts(
    contacts: List<Contact>,
    query: String,
): List<Contact> {
    val normalizedQuery = query.trim().lowercase()
    if (normalizedQuery.isEmpty()) return contacts
    return contacts.filter { contact ->
        contact.name.lowercase().contains(normalizedQuery) ||
            contact.phone.lowercase().contains(normalizedQuery)
    }
}

internal fun buildContactSections(contacts: List<Contact>): List<ContactSection> {
    return contacts
        .groupBy { it.sectionInitial }
        .map { (initial, sectionContacts) ->
            ContactSection(
                initial = initial,
                contacts = sectionContacts.sortedBy { it.name.lowercase() },
            )
        }
        .sortedWith(compareBy({ it.initial == "#" }, { it.initial }))
}

internal fun nearestAlphabetIndex(progress: Float): Int =
    progress.roundToInt().coerceIn(0, 25)

internal fun bezierFocusAmount(
    distanceFromFocus: Float,
    radius: Float,
): Float {
    val normalizedDistance = (abs(distanceFromFocus) / radius).coerceIn(0f, 1f)
    return 1f - 3f * normalizedDistance * normalizedDistance +
        2f * normalizedDistance * normalizedDistance * normalizedDistance
}

internal fun alphabetLensValue(
    lineValue: Float,
    lensValue: Float,
    lensAmount: Float,
): Float = lerp(lineValue, lensValue, lensAmount.coerceIn(0f, 1f))

internal fun alphabetScale(
    distanceFromFocus: Float,
    radius: Float,
    selectedScale: Float = 2.6f,
): Float = lerp(1f, selectedScale, bezierFocusAmount(distanceFromFocus, radius))

internal fun alphabetBezierX(
    distanceFromFocus: Float,
    radius: Float,
    amplitude: Float,
): Float = amplitude * bezierFocusAmount(distanceFromFocus, radius)

internal fun alphabetBezierY(
    baseY: Float,
    focusY: Float,
    distanceFromFocus: Float,
    radius: Float,
): Float {
    val focus = bezierFocusAmount(distanceFromFocus, radius)
    return baseY + focus * (focusY - baseY) * 0.12f
}

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onBack: () -> Unit,
    onAddContact: (name: String, phone: String) -> Boolean,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val filteredContacts = remember(contacts, searchQuery) {
        filterContacts(contacts, searchQuery)
    }
    val sections = remember(filteredContacts) {
        buildContactSections(filteredContacts)
    }
    val sectionInitials = remember(sections) {
        sections.map { it.initial }.toSet()
    }
    val sectionPositions = remember(sections) {
        val positions = mutableMapOf<String, Int>()
        var itemIndex = 0
        sections.forEach { section ->
            positions[section.initial] = itemIndex
            itemIndex += 1 + section.contacts.size
        }
        positions
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = Ink,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.title_contacts),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.action_add_contact),
                        tint = CheckoutBlue,
                    )
                }
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.hint_search_contacts)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 72.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (sections.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.empty_contacts),
                            color = Gray400,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                }
                sections.forEach { section ->
                    item(key = "section-${section.initial}") {
                        ContactSectionHeader(section.initial)
                    }
                    items(
                        items = section.contacts,
                        key = { contact -> "contact-${contact.id}" },
                    ) { contact ->
                        ContactRow(contact)
                    }
                }
            }
        }
        CurvedAlphabetIndex(
            availableInitials = sectionInitials,
            onLetterSelected = { letter ->
                val sectionIndex = sectionPositions[letter]
                if (sectionIndex != null && sectionIndex >= 0) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(sectionIndex)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(2f / 3f)
                .width(64.dp),
        )
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAddContact = onAddContact,
        )
    }
}

@Composable
private fun ContactSectionHeader(initial: String) {
    Text(
        text = initial,
        color = CheckoutBlue,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun ContactRow(contact: Contact) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = GroupBlue.copy(alpha = 0.12f),
            ) {
                Text(
                    text = contact.name.firstOrNull()?.uppercase()?.toString() ?: "#",
                    color = GroupBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column {
                Text(
                    text = contact.name,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = contact.phone,
                    color = Gray400,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun CurvedAlphabetIndex(
    availableInitials: Set<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectionProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var isDragging by rememberSaveable { mutableStateOf(false) }
    var hasSelection by rememberSaveable { mutableStateOf(false) }
    var indexSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val verticalPaddingPx = with(density) { 12.dp.toPx() }
    val letters = remember { ('A'..'Z').map { it.toString() } }
    val itemSpacing = if (indexSize.height > verticalPaddingPx * 2f) {
        (indexSize.height - verticalPaddingPx * 2f) / letters.lastIndex
    } else {
        0f
    }
    val focusedIndex = nearestAlphabetIndex(selectionProgress)
    val focusedLetter = letters[focusedIndex]

    val snappedProgress by animateFloatAsState(
        targetValue = if (isDragging) {
            selectionProgress
        } else {
            nearestAlphabetIndex(selectionProgress).toFloat()
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "alphabetSnapProgress",
    )
    val displayProgress = if (isDragging) selectionProgress else snappedProgress
    val lensAmount by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "alphabetLensAmount",
    )

    LaunchedEffect(focusedIndex, isDragging, availableInitials) {
        if (hasSelection && !isDragging && focusedLetter in availableInitials) {
            onLetterSelected(focusedLetter)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { indexSize = it }
            .pointerInput(itemSpacing, verticalPaddingPx) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        if (itemSpacing > 0f) {
                            selectionProgress = (
                                (offset.y - verticalPaddingPx) / itemSpacing
                                ).coerceIn(0f, letters.lastIndex.toFloat())
                        }
                    },
                    onDragEnd = {
                        selectionProgress = nearestAlphabetIndex(selectionProgress).toFloat()
                        hasSelection = true
                        isDragging = false
                    },
                    onDragCancel = {
                        selectionProgress = nearestAlphabetIndex(selectionProgress).toFloat()
                        hasSelection = true
                        isDragging = false
                    },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        if (itemSpacing > 0f) {
                            selectionProgress = (
                                (change.position.y - verticalPaddingPx) / itemSpacing
                                ).coerceIn(0f, letters.lastIndex.toFloat())
                        }
                    },
                )
            }
            .drawBehind {
                if (size.width <= 0f || size.height <= 0f) return@drawBehind
                val baseX = size.width * 0.72f
                val amplitude = size.width * 0.22f * lensAmount
                val focusY = verticalPaddingPx + displayProgress * itemSpacing
                val apexX = baseX - amplitude
                val path = Path()
                path.moveTo(baseX, verticalPaddingPx)
                path.cubicTo(
                    baseX,
                    focusY - itemSpacing * 2f,
                    apexX,
                    focusY - itemSpacing,
                    apexX,
                    focusY,
                )
                path.cubicTo(
                    apexX,
                    focusY + itemSpacing,
                    baseX,
                    focusY + itemSpacing * 2f,
                    baseX,
                    size.height - verticalPaddingPx,
                )
                drawPath(
                    path = path,
                    color = GroupBlue.copy(alpha = 0.08f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            },
    ) {
        letters.forEachIndexed { index, letter ->
            val distanceFromFocus = index - displayProgress
            if (indexSize == IntSize.Zero) {
                return@forEachIndexed
            }

            val focusRadius = 2f
            val focusedAmount = bezierFocusAmount(distanceFromFocus, focusRadius)
            val focusAmount = focusedAmount * lensAmount
            val baseX = indexSize.width * 0.72f
            val amplitude = indexSize.width * 0.22f
            val baseY = verticalPaddingPx + index * itemSpacing
            val focusY = verticalPaddingPx + displayProgress * itemSpacing
            val x = baseX - alphabetLensValue(
                lineValue = 0f,
                lensValue = alphabetBezierX(distanceFromFocus, focusRadius, amplitude),
                lensAmount = lensAmount,
            )
            val lensY = alphabetBezierY(baseY, focusY, distanceFromFocus, focusRadius)
            val y = alphabetLensValue(
                lineValue = baseY,
                lensValue = lensY,
                lensAmount = lensAmount,
            ).coerceIn(verticalPaddingPx, indexSize.height - verticalPaddingPx)
                .coerceIn(verticalPaddingPx, indexSize.height - verticalPaddingPx)
            val isAvailable = letter in availableInitials
            val itemAlpha = if (isAvailable) {
                lerp(0.32f, 1f, focusAmount)
            } else {
                lerp(0.10f, 0.35f, focusAmount)
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .graphicsLayer {
                        val scale = alphabetLensValue(
                            lineValue = 1f,
                            lensValue = alphabetScale(distanceFromFocus, focusRadius),
                            lensAmount = lensAmount,
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = itemAlpha
                        translationX = -size.width / 2f
                        translationY = -size.height / 2f
                    },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = letter,
                        color = if (isAvailable) GroupBlue else Gray400,
                        fontSize = 12.sp,
                        fontWeight = if (focusAmount > 0.75f) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    onAddContact: (name: String, phone: String) -> Boolean,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_add_contact)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.label_contact_name)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.label_contact_phone)) },
                    singleLine = true,
                )
                if (showError) {
                    Text(
                        text = stringResource(R.string.error_add_contact),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val added = onAddContact(name, phone)
                    if (added) {
                        onDismiss()
                    } else {
                        showError = true
                    }
                },
            ) {
                Text(stringResource(R.string.action_add_contact))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
