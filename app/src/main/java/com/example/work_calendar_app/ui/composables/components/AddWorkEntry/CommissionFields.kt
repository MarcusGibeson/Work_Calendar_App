package com.example.work_calendar_app.ui.composables.components.AddWorkEntry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.work_calendar_app.viewmodels.WorkViewModel

@Composable
fun CommissionFields(viewModel: WorkViewModel) {
    Column {
        Box (modifier = Modifier.widthIn(max = 150.dp)
        ) {
            OutlinedTextField(
                value = viewModel.commissionRate,
                onValueChange = { viewModel.commissionRate = it },
                label = { Text("Commission Rate") }
            )
        }

        Spacer (modifier = Modifier.height(8.dp))

        SalesList(viewModel)

        Spacer (modifier = Modifier.height(8.dp))

        Box (modifier = Modifier.widthIn(max = 150.dp)
        ) {
            OutlinedTextField(
                value = viewModel.tips,
                onValueChange = { viewModel.tips = it },
                label = { Text("Tips") }
            )
        }

    }
}


@Composable
fun SalesList(viewModel: WorkViewModel) {
    val salesList = remember { viewModel.sales }

    Box(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .padding(top = 16.dp)
    ) {
        Surface(
            border = BorderStroke(1.dp, Color.Gray),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Column (modifier = Modifier.padding(8.dp)) {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(100.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    items(salesList.size) { index ->
                        OutlinedTextField(
                            value = salesList[index],
                            onValueChange = { newSale ->
                                salesList[index] = newSale
                            },
                            label = { Text("Sale ${index + 1}") },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { salesList.add("") }) {
                    Text("Add Sale")
                }
            }
        }

        Text(
            text = "Sales",
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 36.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}