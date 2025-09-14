package com.demo.feature_market.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.demo.feature_market.domain.model.MarketType
import kotlinx.coroutines.FlowPreview

@Composable
fun MarketTabRow(
    selectedType: MarketType,
    onTypeSelected: (MarketType) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = when (selectedType) {
            MarketType.SPOT -> 0
            MarketType.FUTURE -> 1
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Tab(
            selected = selectedType == MarketType.SPOT,
            onClick = { onTypeSelected(MarketType.SPOT) },
            text = {
                Text(
                    text = "Spot",
                    fontWeight = if (selectedType == MarketType.SPOT) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            }
        )
        Tab(
            selected = selectedType == MarketType.FUTURE,
            onClick = { onTypeSelected(MarketType.FUTURE) },
            text = {
                Text(
                    text = "Future",
                    fontWeight = if (selectedType == MarketType.FUTURE) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            }
        )
    }
}

@Preview
@Composable
fun PreviewTabRow() {
    MarketTabRow(selectedType = MarketType.FUTURE, {})
}