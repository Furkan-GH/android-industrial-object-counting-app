package com.plcoding.cameraxguide.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.plcoding.cameraxguide.Detection
import com.plcoding.cameraxguide.LogViewModel
import com.plcoding.cameraxguide.LogViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: LogViewModel = viewModel(factory = LogViewModelFactory(context))
    val logs by viewModel.logs

    var showImage by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    // Sıralama artan mı? (true = eski → yeni, false = yeni → eski)
    var isAscending by remember { mutableStateOf(true) }

    // logs listesini sıralıyoruz
    val sortedLogs = remember(logs, isAscending) {
        logs.sortedWith(compareBy {
            // timestamp'ı parse edip Date objesi alıyoruz
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            parser.parse(it.timestamp)
        }).let { if (isAscending) it else it.reversed() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tespit kayıtları") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        //Text("Tarih", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier
                                .weight(1.5f)
                                .clickable {
                                    isAscending = !isAscending
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tarih", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isAscending)
                                    Icons.Default.ArrowDropDown
                                else
                                    Icons.Default.ArrowDropUp,
                                contentDescription = if (isAscending) "Artan sıralama" else "Azalan sıralama"
                            )
                        }
                        Text("Nesne", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Text("Sayı", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                        Text("Görüntü", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    }
                    Divider()
                }
                items(sortedLogs) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val (formattedDate, formattedTime) = formatTimestamp(log.timestamp)
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(text = formattedDate, style = MaterialTheme.typography.bodyLarge)
                            Text(text = formattedTime, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(log.object_type, modifier = Modifier.weight(1f))
                        Text(log.detected_count.toString(), modifier = Modifier.weight(1f))

                        Button(onClick = {
                            selectedImageUrl = "http://raspery.duckdns.org:8000/" + log.image_url
                            showImage = true
                        },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aç")
                        }
                    }
                    Divider()
                }
            }
        }

        if (showImage) {
            AlertDialog(
                onDismissRequest = { showImage = false },
                confirmButton = {
                    TextButton(onClick = { showImage = false }) {
                        Text("Kapat")
                    }
                },
                title = { Text("Tespit edilmiş görüntü") },
                text = {
                    AsyncImage(
                        model = selectedImageUrl,
                        contentDescription = "Tespit edilmiş görüntü",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
            )
        }
    }
}
fun formatTimestamp(isoTimestamp: String): Pair<String, String> {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val date = parser.parse(isoTimestamp)

        // İstenen format: gün.ay.yıl
        val dateFormatter = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())

        // Sadece saat:dakika (saniye yok)
        val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

        val formattedDate = dateFormatter.format(date ?: return Pair(isoTimestamp, ""))
        val formattedTime = timeFormatter.format(date)

        Pair(formattedDate, formattedTime)
    } catch (e: Exception) {
        Pair(isoTimestamp, "")
    }
}


