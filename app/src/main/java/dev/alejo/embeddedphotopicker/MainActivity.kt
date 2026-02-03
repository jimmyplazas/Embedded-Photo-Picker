@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPhotoPickerComposeApi::class)

package dev.alejo.embeddedphotopicker

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.photopicker.EmbeddedPhotoPickerFeatureInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.photopicker.compose.EmbeddedPhotoPicker
import androidx.photopicker.compose.ExperimentalPhotoPickerComposeApi
import androidx.photopicker.compose.rememberEmbeddedPhotoPickerState
import coil3.compose.AsyncImage
import dev.alejo.embeddedphotopicker.ui.theme.EmbeddedPhotoPickerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresExtension(extension = Build.VERSION_CODES.UPSIDE_DOWN_CAKE, version = 15)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmbeddedPhotoPickerTheme {
                var attachments by remember { mutableStateOf(emptyList<Uri>()) }

                val scope = rememberCoroutineScope()

                val scaffoldState = rememberBottomSheetScaffoldState(
                    bottomSheetState = rememberStandardBottomSheetState(
                        initialValue = SheetValue.Hidden,
                        skipHiddenState = false
                    )
                )

                val photoPickerInfo = EmbeddedPhotoPickerFeatureInfo
                    .Builder()
                    .setMaxSelectionLimit(5)
                    .setOrderedSelection(true)
                    .build()

                val photoPickerState = rememberEmbeddedPhotoPickerState(
                    onSelectionComplete = {
                        scope.launch {
                            scaffoldState.bottomSheetState.hide()
                        }
                    },
                    onUriPermissionGranted = {
                        attachments += it
                    },
                    onUriPermissionRevoked = {
                        attachments -= it
                    }
                )

                SideEffect {
                    val isExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
                    photoPickerState.setCurrentExpanded(isExpanded)
                }

                BottomSheetScaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = "Embedded Photo Picker Example") })
                    },
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = if(scaffoldState.bottomSheetState.isVisible) 300.dp else 0.dp,
                    sheetContent = {
                        EmbeddedPhotoPicker(
                            state = photoPickerState,
                            embeddedPhotoPickerFeatureInfo = photoPickerInfo,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            }
                        ) {
                            Text(text = "Open Photo Picker")
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 80.dp)
                        ) {
                            items(attachments) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .clickable {
                                            scope.launch {
                                                photoPickerState.deselectUri(uri)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}