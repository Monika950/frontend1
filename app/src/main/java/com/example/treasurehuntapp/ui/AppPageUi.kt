package com.example.treasurehuntapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AppPageUi {
    val HeaderHorizontalPadding: Dp = 16.dp
    val HeaderVerticalPadding: Dp = 12.dp
}

fun Modifier.appPageHeaderPadding(): Modifier =
    this
        .statusBarsPadding()
        .padding(
            horizontal = AppPageUi.HeaderHorizontalPadding,
            vertical = AppPageUi.HeaderVerticalPadding
        )
