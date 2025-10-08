package com.example.skolar20.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.skolar20.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkolarTopBar(title: String) {
    CenterAlignedTopAppBar(
        title = {
            Row {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Skolar logo"
                )
                Spacer(Modifier.width(8.dp))
                Text(title)
            }
        }
    )
}
