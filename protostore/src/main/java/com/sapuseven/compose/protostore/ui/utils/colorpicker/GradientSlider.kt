package com.sapuseven.compose.protostore.ui.utils.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HorizontalGradientSlider(
	brush: Brush,
	value: Float,
	onValueChanged: (Float) -> Unit,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	thumbColor: Color,
	modifier: Modifier = Modifier,
) {
	val sliderColors = SliderDefaults.colors(thumbColor = thumbColor)
	val interactionSource = remember { MutableInteractionSource() }

	Slider(
		value = value,
		onValueChange = {
			onValueChanged(it)
		},
		valueRange = valueRange,
		colors = sliderColors,
		interactionSource = interactionSource,
		thumb = {
			SliderDefaults.Thumb(
				interactionSource = interactionSource,
				colors = sliderColors,
			)
		},
		track = {
			SliderDefaults.Track(
				sliderState = it,
				drawStopIndicator = null,
				colors = sliderColors,
				modifier = Modifier
					.then(DrawWithBrush(brush, LocalContentColor.current))
			)
		},
		modifier = modifier
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VerticalGradientSlider(
	brush: Brush,
	value: Float,
	thumbColor: Color,
	onValueChanged: (Float) -> Unit,
	valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
	modifier: Modifier = Modifier,
) = HorizontalGradientSlider(
	brush = brush,
	value = value,
	thumbColor = thumbColor,
	onValueChanged = onValueChanged,
	valueRange = valueRange,
	modifier = modifier
        .graphicsLayer {
            rotationZ = 270f
            transformOrigin = TransformOrigin(0f, 0f)
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(
                Constraints(
                    minWidth = constraints.minHeight,
                    maxWidth = constraints.maxHeight,
                    minHeight = constraints.minWidth,
                    maxHeight = constraints.maxHeight,
                )
            )
            layout(placeable.height, placeable.width) {
                placeable.place(-placeable.width, 0)
            }
        }
)

private class DrawWithBrush(
	private val brush: Brush,
	private val contentColor: Color
) : DrawModifier {
	override fun ContentDrawScope.draw() {
		drawIntoCanvas {
			it.saveLayer(size.toRect(), Paint().apply { blendMode = BlendMode.SrcOver } )

			drawContent()

			it.saveLayer(size.toRect(), Paint().apply { blendMode = BlendMode.SrcIn } )
			drawRect(Color.White, blendMode = BlendMode.Clear)
			drawCheckeredBackground(
				gridSizePx = size.height / 4,
				darkColor = contentColor.copy(alpha = .1f),
				lightColor = contentColor.copy(alpha = .2f),
				blendMode = BlendMode.Src
			)
			drawRect(brush, blendMode = BlendMode.SrcOver)
			it.restore()

			it.restore()
		}
	}
}

private fun DrawScope.drawCheckeredBackground(
	gridSizePx: Float,
	darkColor: Color,
	lightColor: Color,
	blendMode: BlendMode
) {
	val cellCountX = ceil(this.size.width / gridSizePx).toInt()
	val cellCountY = ceil(this.size.height / gridSizePx).toInt()
	for (i in 0 until cellCountX) {
		for (j in 0 until cellCountY) {
			val color = if ((i + j) % 2 == 0) darkColor else lightColor

			val x = i * gridSizePx
			val y = j * gridSizePx
			drawRect(color, Offset(x, y), Size(gridSizePx, gridSizePx), blendMode = blendMode)
		}
	}
}

@Preview
@Composable
fun GradientSlider_AlphaPreview() {
	var color = remember { HsvColor.from(Color(0x606BFF00)) }

	HorizontalGradientSlider(
		brush = Brush.linearGradient(listOf(color.copy(alpha = 1f).toColor(), color.copy(alpha = 0f).toColor())),
		value = color.alpha,
		onValueChanged = { color = color.copy(alpha = it) },
		thumbColor = color.copy(alpha = 1f).toColor(),
		modifier = Modifier
	)
}

@Preview
@Composable
fun GradientSlider_HuePreview() {
	var color = remember { HsvColor.from(Color(0x606BFF00)) }

	VerticalGradientSlider(
		brush = Brush.linearGradient(
			listOf(
				Color.Red,
				Color.Yellow,
				Color.Green,
				Color.Cyan,
				Color.Blue,
				Color.Magenta,
				Color.Red
			)
		),
		value = color.hue,
		onValueChanged = { color = color.copy(hue = it) },
		valueRange = 0f..360f,
		thumbColor = color.copy(alpha = 1f, saturation = 1f, value = 1f).toColor(),
		modifier = Modifier.height(320.dp)
	)
}
