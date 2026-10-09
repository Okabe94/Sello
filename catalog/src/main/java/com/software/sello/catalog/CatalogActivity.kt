package com.software.sello.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.software.sello.designsystem.theme.SelloTheme

class CatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelloTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatalogHome(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CatalogHome(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.catalog_name),
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = stringResource(R.string.catalog_empty))
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogHomePreview() {
    SelloTheme {
        CatalogHome()
    }
}
