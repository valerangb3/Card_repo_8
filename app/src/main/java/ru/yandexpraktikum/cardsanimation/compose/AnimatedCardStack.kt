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
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }


    // TODO: [Задание 2] Добавьте обработку жестов (+)
    // Подсказка: Используйте Modifier.pointerInput() с методом detectDragGestures()

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            // TODO проверить знак verticalDragOffset чтобы определить swipe был вниз или вверх
                            val threshold = 100f
                            val isVerticalDominant = abs(verticalDragOffset) > abs(horizontalDragOffset)
                            val isHorizontalDominant = abs(horizontalDragOffset) > abs(verticalDragOffset)
                            when {
                                isVerticalDominant && abs(verticalDragOffset) > threshold -> {
                                    handleVerticalDragEnd(
                                        verticalDragOffset = verticalDragOffset,
                                        onChangeRotate = { newState ->
                                            isRotated = newState
                                            animationState = animationState.copy(isAnimating = true)
                                        }
                                    )
                                }
                                isHorizontalDominant && abs(horizontalDragOffset) > threshold -> {
                                    animationState = animationState.copy(animationStep = 1, isAnimating = true)
                                }
                            }
                            verticalDragOffset = 0f
                            horizontalDragOffset = 0f
                            // animationState = CardSwapAnimationState()
                        }
                    }
                ) { change, dragAmount ->
                    change.consume()
                    horizontalDragOffset += dragAmount.x
                    verticalDragOffset += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        cards.forEachIndexed { i, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(i, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = i,
                    cardData = cardData,
                    targetRotation = targetRotation,
                    animationState = if (i == 0) animationState else CardSwapAnimationState(),
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step = step,
                            cardIndex = i,
                            onStepChange = {
                                val isAnimating = it > 0
                                animationState = animationState.copy(animationStep = it, isAnimating = isAnimating)
                            },
                            onAnimationComplete = { animationState = CardSwapAnimationState() }
                        )
                        Log.d("CHECK_HORIZONTAL_OFFSET", "onAnimationStepComplete $animationState")
                    }
                    // TODO: [Задание 5] Здесь добавьте параметры анимации карты
                )
            }
        }
    }
}

private fun handleAnimationStepComplete(
    step: Int,
    cardIndex: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            1 -> onStepChange(2)
            2 -> onStepChange(3)
            3 -> onAnimationComplete()
        }
    }
}

private fun handleVerticalDragEnd(verticalDragOffset: Float, onChangeRotate: (Boolean) -> Unit) {
    when {
        verticalDragOffset < 0 -> {
            Log.d("handleDragEnd", "swipe up")
            onChangeRotate(true)
        }

        verticalDragOffset > 0 -> {
            // swipe down
            Log.d("handleDragEnd", "swipe down")
            onChangeRotate(false)
        }
    }
}

private fun handleHorizontalDragEnd() {

}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}