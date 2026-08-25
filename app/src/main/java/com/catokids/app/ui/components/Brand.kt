package com.catokids.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.catokids.app.R

/** The Cato Kids tree. The app's identity — used at brand moments, not as decoration. */
@Composable
fun CatoKidsLogo(modifier: Modifier = Modifier.size(140.dp)) {
    Image(
        painter = painterResource(R.drawable.logo_cato_kids),
        contentDescription = "Cato Kids",
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** The Mother Goose Learning lockup, duck plus wordmark. Marks the curriculum. */
@Composable
fun MotherGooseLogo(modifier: Modifier = Modifier.height(84.dp)) {
    Image(
        painter = painterResource(R.drawable.logo_mother_goose),
        contentDescription = "Mother Goose Learning",
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/**
 * Mother Goose on her own, without the wordmark — she is the character who reacts
 * to the child: cheering a right answer, waiting patiently on a wrong one.
 */
@Composable
fun GooseCharacter(
    modifier: Modifier = Modifier.size(110.dp),
    cheering: Boolean = false,
    animate: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "goose")
    val bob by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animate) 1f else 0f,
        animationSpec = infiniteRepeatable(
            tween(if (cheering) 620 else 1600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "bob",
    )
    Image(
        painter = painterResource(R.drawable.cato_goose),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .scale(1f + (if (cheering) 0.06f else 0.02f) * bob)
            .rotate(if (cheering) (bob - 0.5f) * 7f else 0f),
    )
}
