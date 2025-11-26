package ru.yandexpraktikum.cardsanimation.compose

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var horizontalDragOffset by remember { mutableFloatStateOf(0f) }

    // TODO: [Задание 2] Добавьте обработку жестов (+)
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // TODO проверить знак verticalDragOffset чтобы определить swipe был вниз или вверх
                        handleDragEnd(verticalDragOffset = verticalDragOffset) { newState ->
                            isRotated = newState
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val x = dragAmount.x
                    val y = dragAmount.y
                    // TODO переделать, чтобы суммировать verticalDragOffset и horizontalDragOffset
                    // TODO а в onDragEnd испльзовать эти значения, чтобы определять horizontal или vertical swipe
                    if (abs(x) > abs(y)) {
                        // todo horizontal swipe
                        // reorderCards(cards = cards)
                        horizontalDragOffset += x
                        Log.d("HORIZONTAL", "HORIZONTAL")
                    } else {
                        verticalDragOffset += y
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    onClick = {
                        isRotated = !isRotated
                    }
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

private fun handleDragEnd(verticalDragOffset: Float, onChangeRotate: (Boolean) -> Unit) {
    val threshold = 100f
    when {
        verticalDragOffset < -threshold -> {
            // swipe up
            Log.d("handleDragEnd", "swipe up")
            onChangeRotate(true)
        }

        verticalDragOffset > threshold -> {
            // swipe down
            Log.d("handleDragEnd", "swipe down")
            onChangeRotate(false)
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}