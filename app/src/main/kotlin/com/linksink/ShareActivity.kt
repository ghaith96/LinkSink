package com.linksink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.linksink.ui.ShareScreen
import com.linksink.ui.theme.LinkSinkTheme
import com.linksink.ui.theme.Spacing
import com.linksink.viewmodel.ShareViewModel

class ShareActivity : ComponentActivity() {

    private lateinit var viewModel: ShareViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LinkSinkApp
        viewModel = ShareViewModel(
            repository = app.repository,
            topicRepository = app.topicRepository
        )

        val sharedText = when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    intent.getStringExtra(Intent.EXTRA_TEXT)
                } else null
            }
            else -> null
        }

        viewModel.processSharedText(sharedText)

        setContent {
            LinkSinkTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier.padding(Spacing.lg)
                    ) {
                        ShareScreen(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSuccess = { finish() }
                        )
                    }
                }
            }
        }
    }
}
