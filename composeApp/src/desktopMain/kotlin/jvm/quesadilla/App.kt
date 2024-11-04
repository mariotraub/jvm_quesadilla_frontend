package jvm.quesadilla

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jvm.quesadilla.file.File
import jvm.quesadilla.file.Id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val json = Json { isLenient = true; ignoreUnknownKeys = true }

@Composable
@Preview
fun App() {
    val textState = remember { mutableStateOf("") }
    val directoryState = remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<File>() }
    val coroutineScope = rememberCoroutineScope()
    val buttonEnabled = remember { mutableStateOf(true) }
    MaterialTheme {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = textState.value,
                onValueChange = {
                    textState.value = it
                    val results = searchFiles(it)
                    searchResults.clear()
                    searchResults.addAll(results)
                    print(results)
                },
                label = { Text("Suche nach einer Datei") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (searchResults.isEmpty()) {
                    Text(text = "Keine Ergebnisse")
                }
                searchResults.forEach { result ->
                    Text(text = "${result.type}: ${result.path}")
                }
            }

            TextField(
                value = directoryState.value,
                onValueChange = { directoryState.value = it },
                label = { Text("Verzeichis zu cachen") },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        buttonEnabled.value = false
                        startCaching(directoryState.value)
                        buttonEnabled.value = true
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                enabled = buttonEnabled.value
            ) {
                Text("Starte Caching")
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun searchFiles(query: String): List<File> {
    return try {
        val queryBase64 = Base64.encode(query.toByteArray())
        val url = URI("http://localhost:8080/file/${queryBase64}").toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.readLines()
        }
        json.decodeFromString<List<File>>(response.joinToString("\n"))
    } catch (e: Exception) {
        System.err.println(e.message)
        emptyList()
    }
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun startCaching(query: String) {
    withContext(Dispatchers.IO) {
        try {
            val queryBase64 = Base64.encode(query.toByteArray())
            val url = URI("http://localhost:8080/cache/update/${queryBase64}").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                reader.readLines()
            }
        } catch (e: Exception) {
            System.err.println(e.message)
        }
    }
}