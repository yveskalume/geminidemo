package com.yveskalume.geminidemo

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.yveskalume.geminidemo.ui.theme.GeminidemoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    val generativeModel = GenerativeModel(
        modelName = "gemini-pro-vision",
        apiKey = "AIzaSyATYBDY4nkf-MfucZ-iozcRicYBZBF4evg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminidemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var imageUri: Uri? by remember {
                        mutableStateOf(null)
                    }

                    val context = LocalContext.current

                    val photoPicker =
                        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
                            imageUri = uri
                        }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val coroutineScope = rememberCoroutineScope()

                        var prompt by remember {
                            mutableStateOf("")
                        }

                        var result by remember {
                            mutableStateOf("")
                        }

                        var isProcessing by remember {
                            mutableStateOf(false)
                        }

                        TextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = {
                                Text(text = "Entrez votre prompt")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = {
                            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Get images")
                        }

                        Button(onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val imageBitmap = MediaStore.Images.Media.getBitmap(
                                    context.contentResolver,
                                    imageUri
                                )
                                val inputContent = content {
                                    image(imageBitmap)
                                    text(prompt)
                                }
                                val response = generativeModel.generateContent(inputContent)
                                result = response.text.toString()
                                isProcessing = false
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Soumettre")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (imageUri != null) {
                            Image(
                                bitmap = remember(imageUri) {
                                    MediaStore.Images.Media.getBitmap(
                                        context.contentResolver,
                                        imageUri
                                    ).asImageBitmap()
                                },
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedVisibility(isProcessing) {
                            CircularProgressIndicator()
                        }

                        AnimatedVisibility(result.isNotBlank()) {
                            Text(text = result)
                        }
                    }
                }
            }
        }
    }
}