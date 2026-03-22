package com.linksink.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.linksink.data.LinkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow

class SyncManager(
    private val context: Context,
    private val repository: LinkRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isNetworkCallbackRegistered = false

    fun startNetworkObserver() {
        if (isNetworkCallbackRegistered) return

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available, triggering sync")
                triggerSync()
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network lost")
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            isNetworkCallbackRegistered = true
            Log.d(TAG, "Network callback registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    fun stopNetworkObserver() {
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                isNetworkCallbackRegistered = false
                Log.d(TAG, "Network callback unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unregister network callback", e)
            }
        }
        networkCallback = null
    }

    fun triggerSync() {
        if (_isSyncing.value) {
            Log.d(TAG, "Sync already in progress, skipping")
            return
        }

        scope.launch {
            syncWithRetry()
        }
    }

    private suspend fun syncWithRetry() {
        _isSyncing.value = true
        var attempt = 0
        var totalSynced = 0
        var lastError: Throwable? = null

        while (attempt < MAX_SYNC_ATTEMPTS) {
            try {
                val result = repository.syncPendingLinks()
                if (result.isSuccess) {
                    val synced = result.getOrDefault(0)
                    totalSynced += synced

                    val remainingPending = getPendingCountBlocking()
                    if (remainingPending == 0) {
                        _lastSyncResult.value = SyncResult.Success(totalSynced)
                        Log.d(TAG, "Sync complete: $totalSynced links synced")
                        break
                    } else {
                        Log.d(TAG, "Synced $synced links, $remainingPending remaining. Retrying...")
                        attempt++
                        if (attempt < MAX_SYNC_ATTEMPTS) {
                            val backoffMs = calculateBackoff(attempt)
                            Log.d(TAG, "Waiting ${backoffMs}ms before retry")
                            delay(backoffMs)
                        }
                    }
                } else {
                    lastError = result.exceptionOrNull()
                    attempt++
                    if (attempt < MAX_SYNC_ATTEMPTS) {
                        val backoffMs = calculateBackoff(attempt)
                        Log.d(TAG, "Sync failed, waiting ${backoffMs}ms before retry")
                        delay(backoffMs)
                    }
                }
            } catch (e: Exception) {
                lastError = e
                Log.e(TAG, "Sync error on attempt $attempt", e)
                attempt++
                if (attempt < MAX_SYNC_ATTEMPTS) {
                    val backoffMs = calculateBackoff(attempt)
                    delay(backoffMs)
                }
            }
        }

        if (attempt >= MAX_SYNC_ATTEMPTS && lastError != null) {
            _lastSyncResult.value = SyncResult.Failure(
                syncedCount = totalSynced,
                error = lastError.message ?: "Unknown error"
            )
            Log.e(TAG, "Sync failed after $MAX_SYNC_ATTEMPTS attempts")
        }

        _isSyncing.value = false
    }

    private suspend fun getPendingCountBlocking(): Int {
        return try {
            repository.getPendingSyncCount().first()
        } catch (e: Exception) {
            0
        }
    }

    private fun calculateBackoff(attempt: Int): Long {
        val exponentialMs = (BASE_BACKOFF_MS * 2.0.pow(attempt - 1)).toLong()
        return min(exponentialMs, MAX_BACKOFF_MS)
    }

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_SYNC_ATTEMPTS = 5
        private const val BASE_BACKOFF_MS = 1000L
        private const val MAX_BACKOFF_MS = 16000L
    }
}

sealed interface SyncResult {
    data class Success(val syncedCount: Int) : SyncResult
    data class Failure(val syncedCount: Int, val error: String) : SyncResult
}
