package com.trever.android.ui.search
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trever.android.ui.theme.G_200
import com.trever.android.ui.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSelectCarModelScreen(
    viewModel: SearchViewModel,
    manufacturer: String,
    carName: String,
    onSystemBack: () -> Unit,
    onCarModelSelected: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val carModels by viewModel.carModels.collectAsState()
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(manufacturer, carName) {
        viewModel.fetchCarModels(manufacturer, carName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.backgroundColor
                ),
                title = { Text(carName, fontWeight = FontWeight.Bold) },
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
                items(carModels) { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCarModelSelected(model.carModel) }
                            .background(cs.backgroundColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.carModel,
                            fontSize = 16.sp,
                            color = cs.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${model.count}",
                            color = cs.G_200,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
