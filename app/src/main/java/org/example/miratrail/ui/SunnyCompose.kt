package org.example.miratrail.ui

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.*
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import org.example.miratrail.R

@Composable
internal fun <T> StateFlow<T>.screenValue(owner: LifecycleOwner): T {
    return collectAsStateWithLifecycle(lifecycleOwner = owner).value
}

@Composable
internal fun SunnyPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val gutter = SunnySpacing.PageGutter
    Column(
        modifier
            .fillMaxSize()
            .background(SunnyColors.Cream)

            .verticalScroll(rememberScrollState())
            .padding(start = gutter, end = gutter, top = 8.dp),
        content = content
    )
}

@Composable
internal fun SunnyText(
    text: String,
    style: SunnyTextStyle = SunnyTextStyle.Body,
    modifier: Modifier = Modifier,
    padding: Dp = 0.dp,
    background: Color? = null,
    color: Color = SunnyColors.Ink
) {
    Text(
        text,
        style = sunnyTextStyle(style, color),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (background != null) Modifier.background(
                    background,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            )
            .padding(padding)
    )
}

@Composable
internal fun SunnyInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    minLines: Int = 1,
    multiline: Boolean = false,
    capitalize: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    val floating = focused || value.isNotEmpty()
    val ink = SunnyColors.Ink
    val surface = SunnyColors.Surface
    val hint = SunnyColors.Hint
    val coral = SunnyColors.Coral
    val errorColor = SunnyColors.Error
    val border = when {
        error != null -> errorColor
        focused -> coral
        else -> SunnyColors.InputOutline
    }
    val density = LocalDensity.current
    val labelInset = remember(density) {
        with(density) {
            val metrics = Paint().apply { textSize = 12.sp.toPx() }.fontMetrics
            (-metrics.ascent / 2).toInt().toDp()
        }
    }
    val fieldStyle = TextStyle(
        color = ink,
        fontFamily = SunnyFonts.Body,
        fontSize = 16.sp,
        // Same letter spacing as the outlined TextInputEditText theme.
        letterSpacing = 0.009375.em,
        lineBreak = LineBreak(
            strategy = LineBreak.Strategy.Balanced,
            strictness = LineBreak.Strictness.Normal,
            wordBreak = LineBreak.WordBreak.Default
        ),
        platformStyle = PlatformTextStyle(includeFontPadding = true)
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = !multiline,
        minLines = minLines,
        textStyle = fieldStyle,
        cursorBrush = SolidColor(coral),
        keyboardOptions = KeyboardOptions(
            capitalization = if (capitalize) KeyboardCapitalization.Sentences
            else KeyboardCapitalization.None
        ),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused }
            .semantics { if (error != null) error(error) },
        decorationBox = { innerTextField ->
            Column {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .padding(top = labelInset)
                            .fillMaxWidth()
                            .background(SunnyColors.Surface, RoundedCornerShape(12.dp))
                            .border(if (focused) 2.dp else 1.dp, border, RoundedCornerShape(12.dp))
                            .heightIn(min = 64.dp)
                            .padding(
                                start = 16.dp,
                                top = 16.dp,
                                end = if (error != null) 48.dp else 16.dp,
                                bottom = 16.dp
                            )
                    ) {
                        if (!floating) {
                            Text(
                                label,
                                style = fieldStyle.copy(color = if (error != null) errorColor else hint)
                            )
                        }
                        innerTextField()
                    }
                    if (error != null) {
                        Image(
                            imageVector = SunnyIcons.InputError,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(errorColor),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = labelInset + 20.dp, end = 12.dp)
                                .size(24.dp)
                        )
                    }
                    if (floating) {
                        Text(
                            label,
                            style = fieldStyle.copy(
                                color = when {
                                    error != null -> errorColor
                                    focused -> coral
                                    else -> hint
                                },
                                fontSize = 12.sp,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .drawBehind {
                                    drawRect(
                                        color = surface,
                                        topLeft = Offset(0f, labelInset.toPx()),
                                        size = Size(
                                            size.width,
                                            if (focused) 2.dp.toPx() else 1.dp.toPx()
                                        )
                                    )
                                }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                if (error != null) {
                    Text(
                        error,
                        style = fieldStyle.copy(color = errorColor, fontSize = 12.sp),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }
    )
}

@Composable
internal fun SunnyCheckbox(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val coral = SunnyColors.Coral
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            Modifier
                .padding(start = 6.dp, end = 6.dp)
                .size(20.dp)
        ) {
            drawRoundRect(
                color = coral,
                topLeft = if (checked) Offset(1.dp.toPx(), 1.dp.toPx())
                else Offset(2.dp.toPx(), 2.dp.toPx()),
                size = if (checked) Size(18.dp.toPx(), 18.dp.toPx())
                else Size(16.dp.toPx(), 16.dp.toPx()),
                cornerRadius = CornerRadius(1.dp.toPx()),
                style = if (checked) Fill else Stroke(2.dp.toPx())
            )
            if (checked) {
                drawLine(
                    Color.White,
                    Offset(5.dp.toPx(), 10.dp.toPx()),
                    Offset(9.dp.toPx(), 14.dp.toPx()),
                    2.dp.toPx()
                )
                drawLine(
                    Color.White,
                    Offset(9.dp.toPx(), 14.dp.toPx()),
                    Offset(15.dp.toPx(), 6.dp.toPx()),
                    2.dp.toPx()
                )
            }
        }
        Text(label, style = sunnyTextStyle(SunnyTextStyle.Caption))
    }
}

@Composable
internal fun SunnyImage(
    resource: Int,
    height: Dp,
    modifier: Modifier = Modifier,
    rounded: Boolean = true,
    crop: Boolean = true
) {
    Image(
        painterResource(resource),
        contentDescription = null,
        contentScale = if (crop) ContentScale.Crop else ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .then(if (rounded) Modifier.clip(RoundedCornerShape(20.dp)) else Modifier)
    )
}

@Composable
internal fun SunnyFooter() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(R.drawable.figma_branch),
            contentDescription = null,
            modifier = Modifier.size(60.dp, 48.dp)
        )
    }
}

@Composable
internal fun SunnyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        border = if (secondary) BorderStroke(1.dp, SunnyColors.ButtonOutline) else null,
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) SunnyColors.Surface else SunnyColors.Coral,
            contentColor = if (secondary) SunnyColors.Ink else SunnyColors.Surface,
            disabledContainerColor = SunnyColors.ButtonDisabled,
            disabledContentColor = SunnyColors.Surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        Text(
            text.toUpperCase(Locale.current),
            fontSize = 14.sp,
            fontFamily = SunnyFonts.Body,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.sp
        )
    }
}
