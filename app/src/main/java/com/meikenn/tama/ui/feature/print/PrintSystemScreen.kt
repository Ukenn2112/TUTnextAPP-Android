package com.meikenn.tama.ui.feature.print

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meikenn.tama.domain.model.NUpType
import com.meikenn.tama.domain.model.PlexType
import com.meikenn.tama.domain.model.PrintResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintSystemScreen(
    onNavigateBack: () -> Unit,
    viewModel: PrintSystemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.handleFileSelection(it) }
    }

    val supportedMimeTypes = arrayOf(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/tiff",
        "application/rtf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/octet-stream"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("印刷システム") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.showResult && uiState.printResult != null) {
                PrintResultContent(
                    result = uiState.printResult!!,
                    onDismiss = {
                        viewModel.reset()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // File selection
                    if (uiState.selectedFile != null) {
                        val file = uiState.selectedFile!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = viewModel.formattedFileSize(file.size),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                TextButton(onClick = {
                                    filePickerLauncher.launch(supportedMimeTypes)
                                }) {
                                    Text("変更")
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { filePickerLauncher.launch(supportedMimeTypes) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ファイルを選択")
                        }
                    }

                    // Print settings (shown after file selection)
                    if (uiState.selectedFile != null) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "印刷設定",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // N-Up
                                SettingLabel("まとめて1枚")
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    NUpType.entries.forEachIndexed { index, nUp ->
                                        SegmentedButton(
                                            selected = uiState.printSettings.nUp == nUp,
                                            onClick = { viewModel.updateNUp(nUp) },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = NUpType.entries.size
                                            )
                                        ) {
                                            Text(nUp.displayName)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Plex
                                SettingLabel("両面印刷")
                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    PlexType.entries.forEachIndexed { index, plex ->
                                        SegmentedButton(
                                            selected = uiState.printSettings.plex == plex,
                                            onClick = { viewModel.updatePlex(plex) },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = PlexType.entries.size
                                            )
                                        ) {
                                            Text(plex.displayName)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Start page
                                SettingLabel("開始ページ")
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.printSettings.startPage.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.width(40.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    OutlinedButton(onClick = {
                                        viewModel.updateStartPage(uiState.printSettings.startPage - 1)
                                    }) { Text("-") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(onClick = {
                                        viewModel.updateStartPage(uiState.printSettings.startPage + 1)
                                    }) { Text("+") }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // PIN
                                SettingLabel("暗証番号（オプション）")
                                OutlinedTextField(
                                    value = uiState.pinCode,
                                    onValueChange = { viewModel.updatePinCode(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("暗証番号を入力") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true
                                )
                                Text(
                                    "※ 暗証番号を設定すると、印刷時に暗証番号の入力が必要になります",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Upload button
                        Button(
                            onClick = { viewModel.uploadFile() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                "アップロード",
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Recent uploads (shown when no file is selected)
                    if (uiState.selectedFile == null && uiState.recentUploads.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        RecentUploadsSection(uiState.recentUploads)
                    }

                    // Error message
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("処理中...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun RecentUploadsSection(uploads: List<PrintResult>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "最近のアップロード",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            uploads.forEach { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = "予約番号: ${result.printNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = result.formattedExpiryDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrintResultContent(
    result: PrintResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "印刷ファイルのアップロードが完了しました",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            "以下の情報を確認してください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Print number with copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "プリント予約番号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(120.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        result.printNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Print Number", result.printNumber))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("copy", style = MaterialTheme.typography.labelSmall)
                    }
                }

                ResultRow("ファイル名", result.fileName)
                ResultRow("有効期限", result.formattedExpiryDate)
                ResultRow("ページ数", result.pageCount.toString())
                ResultRow("両面", result.duplex)
                ResultRow("サイズ", result.fileSize)
                ResultRow("まとめて1枚", result.nUp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("閉じる", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.width(120.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End
        )
    }
}
