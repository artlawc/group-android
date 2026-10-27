package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.component.SectionTitle
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White
import com.groupa.domain.model.Deal
import com.groupa.presentation.component.DealCard
import com.groupa.coreui.R

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun HomeScreen(
    deals: List<Deal>,
    locationLabel: String,
    onDealClick: (String) -> Unit,
    onLotteryClick: () -> Unit,
    onLocationClick: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val categories = listOf("All", "Dining", "Beauty", "Activities", "Services", "Shopping")
    val filteredDeals = deals.filter { deal ->
        (selectedCategory == "All" || deal.category.equals(selectedCategory, true)) &&
            (query.isBlank() || deal.title.contains(query, true) || deal.merchant.contains(query, true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Explore Deals",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Ink,
                    )
                    Surface(
                        onClick = onLocationClick,
                        color = Color.Transparent,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = GroupBlue,
                                modifier = Modifier.size(17.dp),
                            )
                            Text(
                                text = locationLabel,
                                color = Gray400,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
                Surface(
                    onClick = onLotteryClick,
                    shape = RoundedCornerShape(18.dp),
                    color = GroupBlue.copy(alpha = 0.08f),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = stringResource(R.string.action_open_lottery),
                        tint = GroupBlue,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(22.dp),
                    )
                }
            }
        }
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppBackground),
            ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                placeholder = {
                    Text(text = "Search deals, brands and categories")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Gray400,
                    )
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White,
                    focusedBorderColor = GroupBlue,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categories.forEach { category ->
                    val selected = category == selectedCategory
                    Surface(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) GroupBlue else White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) GroupBlue else Color(0xFFE5E7EB)),
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                            color = if (selected) White else Gray400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            }
        }
        item {
            SectionTitle(
                text = "Featured Deals",
                actionText = "See all",
                onActionClick = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        items(filteredDeals.size) { index ->
            DealCard(
                deal = filteredDeals[index],
                onClick = { onDealClick(filteredDeals[index].id) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
        if (filteredDeals.isEmpty()) {
            item {
                Text(
                    text = "No deals match your search.",
                    color = Gray400,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
