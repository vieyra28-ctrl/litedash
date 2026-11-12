// Decoración de temporada: qué fecha es qué, sus colores y el adorno del inicio.
package com.vieyra28.litekia

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/** La temporada que vale para [this]: Automática sigue la fecha de hoy; las demás, ellas mismas. */
fun SeasonTheme.resolve(today: Day = Day.today()): SeasonTheme {
    if (this != SeasonTheme.AUTO) return this
    val m = today.month
    val d = today.day
    return when {
        // Independencia: vísperas del 15 y 16 de septiembre.
        m == 9 && d in 14..16 -> SeasonTheme.INDEPENDENCE
        // Halloween: la última semana de octubre.
        m == 10 && d >= 25 -> SeasonTheme.HALLOWEEN
        // Día de Muertos: 1 y 2 de noviembre.
        m == 11 && d <= 2 -> SeasonTheme.DAY_OF_DEAD
        // Navidad: desde mediados de noviembre hasta el 26 de diciembre.
        (m == 11 && d >= 10) || (m == 12 && d <= 26) -> SeasonTheme.CHRISTMAS
        // Año Nuevo: del 27 de diciembre al 6 de enero.
        (m == 12 && d >= 27) || (m == 1 && d <= 6) -> SeasonTheme.NEW_YEAR
        // San Valentín: del 10 al 14 de febrero.
        m == 2 && d in 10..14 -> SeasonTheme.VALENTINE
        else -> SeasonTheme.NONE
    }
}

/** Los colores de una temporada: el realce de la interfaz, un segundo color y las luces de la guirnalda. */
data class SeasonPalette(
    val accent: Color,
    val secondary: Color,
    val garland: List<Color>,
)

/**
 * La paleta de la temporada; null si no hay decoración (Ninguna, o Automática sin
 * temporada). Automática debe resolverse antes ([resolve]).
 */
fun SeasonTheme.palette(): SeasonPalette? = when (this) {
    SeasonTheme.NONE, SeasonTheme.AUTO -> null
    SeasonTheme.CHRISTMAS -> SeasonPalette(
        accent = Color(0xFFE8384F),
        secondary = Color(0xFF2EAB5E),
        garland = listOf(Color(0xFFE8384F), Color(0xFF2EAB5E), Color(0xFFF5C542), Color(0xFF4DA3FF), Color(0xFFF4F6F8)),
    )
    SeasonTheme.HALLOWEEN -> SeasonPalette(
        accent = Color(0xFFFF7A1A),
        secondary = Color(0xFF9B5DE5),
        garland = listOf(Color(0xFFFF7A1A), Color(0xFF9B5DE5), Color(0xFF2EAB5E), Color(0xFFF4F6F8)),
    )
    SeasonTheme.DAY_OF_DEAD -> SeasonPalette(
        accent = Color(0xFFFF9F1C),
        secondary = Color(0xFF7B2CBF),
        garland = listOf(Color(0xFFFF9F1C), Color(0xFFFFD60A), Color(0xFF7B2CBF), Color(0xFFE8384F)),
    )
    SeasonTheme.NEW_YEAR -> SeasonPalette(
        accent = Color(0xFFE8B93C),
        secondary = Color(0xFFF4F6F8),
        garland = listOf(Color(0xFFE8B93C), Color(0xFFF4F6F8), Color(0xFF4DA3FF), Color(0xFFE8384F)),
    )
    SeasonTheme.VALENTINE -> SeasonPalette(
        accent = Color(0xFFFF4F9A),
        secondary = Color(0xFFE8384F),
        garland = listOf(Color(0xFFFF4F9A), Color(0xFFE8384F), Color(0xFFF4F6F8), Color(0xFF9B5DE5)),
    )
    SeasonTheme.INDEPENDENCE -> SeasonPalette(
        accent = Color(0xFF1E9E5A),
        secondary = Color(0xFFD4242C),
        garland = listOf(Color(0xFF1E9E5A), Color(0xFFF4F6F8), Color(0xFFD4242C)),
    )
}

/** La temporada activa para estos ajustes (Automática sigue la fecha de hoy). */
fun Preferences.activeSeason(today: Day = Day.today()): SeasonTheme = seasonTheme.resolve(today)

/** El color de realce de la temporada activa, o null si no hay decoración. */
fun Preferences.seasonAccent(today: Day = Day.today()): Color? = activeSeason(today).palette()?.accent

/**
 * El adorno de temporada sobre el inicio: una guirnalda colgando del borde de arriba, con
 * luces de los colores de la temporada, y un motivo en las esquinas (copos en Navidad,
 * corazones en San Valentín, calabazas en Halloween). Estático y tenue, para no distraer
 * al manejar; se dibuja detrás de la barra de arriba, así el texto queda por encima.
 */
@Composable
fun SeasonalDecoration(theme: SeasonTheme, modifier: Modifier = Modifier) {
    val palette = theme.palette() ?: return
    Canvas(modifier.fillMaxWidth().statusBarsPadding().height(48.dp)) {
        val w = size.width
        val h = size.height
        // La guirnalda cuelga en un arco: de esquina a esquina, más baja en el centro.
        val top = 4.dp.toPx()
        val sag = h * 0.5f
        val p0 = Offset(0f, top)
        val p1 = Offset(w / 2, top + 2 * sag)
        val p2 = Offset(w, top)
        fun bezier(t: Float): Offset {
            val u = 1 - t
            return Offset(
                u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x,
                u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y,
            )
        }
        val string = Color.White.copy(alpha = 0.35f)
        val steps = 48
        var prev = bezier(0f)
        for (i in 1..steps) {
            val pt = bezier(i / steps.toFloat())
            drawLine(string, prev, pt, strokeWidth = 1.5.dp.toPx())
            prev = pt
        }
        // Luces colgando de la guirnalda, de los colores de la temporada.
        val bulbR = 4.5.dp.toPx()
        val drop = 7.dp.toPx()
        val bulbs = 14
        for (i in 0 until bulbs) {
            val pt = bezier((i + 0.5f) / bulbs)
            val color = palette.garland[i % palette.garland.size]
            drawLine(string, pt, pt + Offset(0f, drop), strokeWidth = 1.dp.toPx())
            val center = pt + Offset(0f, drop + bulbR)
            drawCircle(color, bulbR, center)
            drawCircle(Color.White.copy(alpha = 0.55f), bulbR * 0.35f, center + Offset(-bulbR * 0.3f, -bulbR * 0.3f))
        }
        // El motivo de cada temporada, en las esquinas (donde la guirnalda no llega).
        when (theme) {
            SeasonTheme.CHRISTMAS -> {
                // Copos de nieve: tres líneas cruzadas (seis brazos).
                val flake = Color.White.copy(alpha = 0.5f)
                val r = 9.dp.toPx()
                val stroke = 1.5.dp.toPx()
                for ((fx, fy) in listOf(0.07f to 0.55f, 0.93f to 0.5f, 0.5f to 0.16f)) {
                    val cx = w * fx
                    val cy = h * fy
                    for (k in 0 until 3) {
                        val a = k * (Math.PI / 3).toFloat()
                        val dx = cos(a) * r
                        val dy = sin(a) * r
                        drawLine(flake, Offset(cx - dx, cy - dy), Offset(cx + dx, cy + dy), strokeWidth = stroke)
                    }
                }
            }
            SeasonTheme.VALENTINE -> {
                // Corazones: dos círculos arriba y un triángulo abajo.
                val heart = palette.accent.copy(alpha = 0.75f)
                for ((fx, fy) in listOf(0.08f to 0.5f, 0.92f to 0.45f)) {
                    val cx = w * fx
                    val cy = h * fy
                    val s = 8.dp.toPx()
                    drawCircle(heart, s * 0.45f, Offset(cx - s * 0.4f, cy - s * 0.25f))
                    drawCircle(heart, s * 0.45f, Offset(cx + s * 0.4f, cy - s * 0.25f))
                    val tri = Path().apply {
                        moveTo(cx - s * 0.8f, cy - s * 0.05f)
                        lineTo(cx + s * 0.8f, cy - s * 0.05f)
                        lineTo(cx, cy + s * 0.85f)
                        close()
                    }
                    drawPath(tri, heart)
                }
            }
            SeasonTheme.HALLOWEEN -> {
                // Calabazas: un círculo naranja con su tallo verde.
                val pumpkin = palette.accent.copy(alpha = 0.8f)
                val stem = Color(0xFF2EAB5E)
                for ((fx, fy) in listOf(0.08f to 0.55f, 0.92f to 0.5f)) {
                    val cx = w * fx
                    val cy = h * fy
                    val r = 9.dp.toPx()
                    drawCircle(pumpkin, r, Offset(cx, cy))
                    drawLine(stem, Offset(cx, cy - r), Offset(cx + 2.dp.toPx(), cy - r - 5.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
            }
            else -> Unit
        }
    }
}
