package com.vehiclemaintenancepro.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.vehiclemaintenancepro.domain.model.VehicleType

@Composable
fun VehicleIllustration(
    vehicleType: VehicleType,
    modifier: Modifier = Modifier,
    bodyColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
) {
    Canvas(modifier = modifier.aspectRatio(1.55f)) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.5f),
                    accentColor.copy(alpha = 0.0f),
                ),
                center = Offset(size.width * 0.52f, size.height * 0.78f),
                radius = size.width * 0.46f,
            ),
            topLeft = Offset(size.width * 0.1f, size.height * 0.58f),
            size = Size(size.width * 0.8f, size.height * 0.26f),
            cornerRadius = CornerRadius(size.height * 0.18f),
        )
        when (vehicleType) {
            VehicleType.Car -> drawCar(bodyColor, accentColor)
            VehicleType.Motorcycle -> drawMotorcycle(bodyColor, accentColor)
        }
    }
}

private fun DrawScope.drawCar(
    bodyColor: Color,
    accentColor: Color,
) {
    val w = size.width
    val h = size.height
    val bodyPath = Path().apply {
        moveTo(w * 0.13f, h * 0.61f)
        cubicTo(w * 0.2f, h * 0.46f, w * 0.35f, h * 0.38f, w * 0.52f, h * 0.39f)
        cubicTo(w * 0.68f, h * 0.4f, w * 0.8f, h * 0.5f, w * 0.88f, h * 0.62f)
        lineTo(w * 0.84f, h * 0.72f)
        lineTo(w * 0.18f, h * 0.72f)
        close()
    }
    drawPath(path = bodyPath, color = bodyColor)
    drawRoundRect(
        color = bodyColor.copy(alpha = 0.92f),
        topLeft = Offset(w * 0.08f, h * 0.62f),
        size = Size(w * 0.84f, h * 0.17f),
        cornerRadius = CornerRadius(h * 0.08f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.62f),
        topLeft = Offset(w * 0.4f, h * 0.44f),
        size = Size(w * 0.2f, h * 0.12f),
        cornerRadius = CornerRadius(h * 0.04f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(w * 0.61f, h * 0.46f),
        size = Size(w * 0.14f, h * 0.1f),
        cornerRadius = CornerRadius(h * 0.04f),
    )
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(w * 0.78f, h * 0.64f),
        size = Size(w * 0.08f, h * 0.035f),
        cornerRadius = CornerRadius(h * 0.02f),
    )
    drawWheel(Offset(w * 0.28f, h * 0.77f), w * 0.085f, bodyColor, accentColor)
    drawWheel(Offset(w * 0.72f, h * 0.77f), w * 0.085f, bodyColor, accentColor)
}

private fun DrawScope.drawMotorcycle(
    bodyColor: Color,
    accentColor: Color,
) {
    val w = size.width
    val h = size.height
    drawWheel(Offset(w * 0.27f, h * 0.74f), w * 0.1f, bodyColor, accentColor)
    drawWheel(Offset(w * 0.75f, h * 0.74f), w * 0.1f, bodyColor, accentColor)
    drawLine(
        color = bodyColor,
        start = Offset(w * 0.34f, h * 0.66f),
        end = Offset(w * 0.58f, h * 0.55f),
        strokeWidth = h * 0.08f,
    )
    drawLine(
        color = bodyColor,
        start = Offset(w * 0.56f, h * 0.55f),
        end = Offset(w * 0.73f, h * 0.7f),
        strokeWidth = h * 0.06f,
    )
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(w * 0.42f, h * 0.47f),
        size = Size(w * 0.22f, h * 0.08f),
        cornerRadius = CornerRadius(h * 0.04f),
    )
    drawLine(
        color = bodyColor,
        start = Offset(w * 0.62f, h * 0.51f),
        end = Offset(w * 0.77f, h * 0.43f),
        strokeWidth = h * 0.035f,
    )
    drawCircle(
        color = accentColor,
        radius = w * 0.035f,
        center = Offset(w * 0.38f, h * 0.61f),
    )
}

private fun DrawScope.drawWheel(
    center: Offset,
    radius: Float,
    bodyColor: Color,
    accentColor: Color,
) {
    drawCircle(color = bodyColor, radius = radius, center = center)
    drawCircle(color = Color.White, radius = radius * 0.58f, center = center)
    drawCircle(color = accentColor, radius = radius * 0.26f, center = center)
}
