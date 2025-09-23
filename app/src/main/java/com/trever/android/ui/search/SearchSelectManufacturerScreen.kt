package com.trever.android.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.common.collect.Multimaps.index
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor
import kotlin.text.category
import kotlin.text.compareTo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSelectManufacturerScreen(
    viewModel: SearchViewModel,
    onSystemBack: () -> Unit,
    onManufacturerSelected: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val categories by viewModel.manufacturerCategories.collectAsState()
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        viewModel.fetchManufacturers()
    }

    Scaffold(

        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor // 원하는 배경색
                ),
                title = { Text("제조사", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onSystemBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로 가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(cs.backgroundColor)
            ) {
                categories.forEachIndexed { index, category ->
                    item {
                        Text(
                            text = category.category,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cs.backgroundColor)
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        )

                    }

                    items(category.manufacturers) { manufacturer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onManufacturerSelected(manufacturer.manufacturer)
                                }
                                .background(cs.backgroundColor)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = manufacturer.manufacturer,
                                fontSize = 16.sp,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${manufacturer.count}",
                                color = cs.G_200,
                                fontSize = 14.sp
                            )
                        }


                    }
                    if (category.category == "국산" && categories.getOrNull(index + 1)?.category == "수입") {
                        item {
                            Divider(
                                color = cs.G_200,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                }
            }
        }
    }
}
