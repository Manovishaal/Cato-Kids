package com.catokids.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.LocalRoleColors
import kotlinx.coroutines.delay

// ---------------------------------------------------------------- buttons

@Composable
fun CatoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    color: Color = LocalRoleColors.current.primary,
    contentColor: Color = Color.White,
    leading: String? = null,
    /** Breathes gently — for the single button you want a child to notice. */
    emphasise: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = modifier
            .pulse(enabled = emphasise && enabled && !loading)
            .bounceOnPress(interaction)
            .heightIn(min = 58.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.5.dp,
                color = contentColor,
                modifier = Modifier.size(22.dp),
            )
        } else {
            if (leading != null) {
                EmojiArt(leading, size = 26.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun CatoOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalRoleColors.current.primary,
    leading: String? = null,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(2.dp, color.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = modifier.heightIn(min = 58.dp),
    ) {
        if (leading != null) {
            EmojiArt(leading, size = 26.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------------------------------------------------------------- inputs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    leading: String? = null,
) {
    var visible by remember { mutableStateOf(!isPassword) }
    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(18.dp),
            leadingIcon = leading?.let { { EmojiArt(it, size = 24.dp) } },
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "Hide" else "Show", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else null,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalRoleColors.current.primary,
                focusedLabelColor = LocalRoleColors.current.primary,
                unfocusedBorderColor = CatoPalette.Line,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(
                error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 14.dp, top = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------- surfaces

@Composable
fun CatoCard(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .then(if (onClick != null) Modifier.bounceOnPress(interaction, pressedScale = 0.975f) else Modifier)
        .shadow(6.dp, RoundedCornerShape(24.dp), clip = false, ambientColor = CatoPalette.Ink.copy(alpha = 0.08f))
        .clip(RoundedCornerShape(24.dp))
        .background(color)
    Column(
        modifier = if (onClick != null) {
            base.clickable(interactionSource = interaction, indication = LocalIndication.current) { onClick() }
        } else {
            base
        },
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = LocalRoleColors.current.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun CatoTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CatoPalette.Ink)
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink, maxLines = 1)
            if (subtitle != null) {
                EmojiText(subtitle, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft, maxLines = 1)
            }
        }
        trailing()
    }
}

// ---------------------------------------------------------------- indicators

@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    max: Int = 3,
    size: Dp = 22.dp,
    /** Off for dense lists, where three stars springing in on every row is noise. */
    animate: Boolean = true,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        repeat(max) { i ->
            val filled = i < stars
            var landed by remember(stars, i) { mutableStateOf(!animate || !filled) }
            LaunchedEffect(stars, i) {
                if (animate && filled) {
                    delay(150L * i + 90L)
                    landed = true
                }
            }
            val scale by animateFloatAsState(
                targetValue = when {
                    !filled -> 0.9f
                    landed -> 1f
                    else -> 0.2f
                },
                animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow),
                label = "starScale$i",
            )
            val spin by animateFloatAsState(
                targetValue = if (!filled || landed) 0f else -60f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
                label = "starSpin$i",
            )
            val tint by animateColorAsState(
                if (filled && landed) CatoPalette.Amber else CatoPalette.Line,
                label = "starTint$i",
            )
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = spin
                    },
            )
        }
    }
}

@Composable
fun CatoProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalRoleColors.current.primary,
    track: Color = CatoPalette.Cloud,
    height: Dp = 12.dp,
    shine: Boolean = true,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "progress",
    )
    val transition = rememberInfiniteTransition(label = "progressShine")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )
    Box(
        modifier
            .height(height)
            .clip(CircleShape)
            .background(track),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.75f), color))),
        ) {
            if (shine && animated > 0.06f) {
                // Stops are derived so they are always strictly ascending.
                val a = (sweep * 1.3f - 0.2f).coerceIn(0f, 0.8f)
                val b = a + 0.1f
                val c = b + 0.1f
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    a to Color.Transparent,
                                    b to Color.White.copy(alpha = 0.35f),
                                    c to Color.Transparent,
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
fun StatPill(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = CatoPalette.AmberSoft,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiArt(emoji, size = 26.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
            Text(label, style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
        }
    }
}

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    color: Color = LocalRoleColors.current.primary,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f))))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
        )
    }
}

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiArt(emoji, size = 50.dp)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = CatoPalette.InkSoft,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(18.dp))
            action()
        }
    }
}

@Composable
fun LoadingBlock(modifier: Modifier = Modifier, label: String = "Loading…") {
    Column(
        modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BouncingDots(color = LocalRoleColors.current.primary)
        Spacer(Modifier.height(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.InkSoft)
    }
}

@Composable
fun InfoBanner(
    emoji: String,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CatoPalette.SkySoft,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiArt(emoji, size = 26.dp)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
    }
}
