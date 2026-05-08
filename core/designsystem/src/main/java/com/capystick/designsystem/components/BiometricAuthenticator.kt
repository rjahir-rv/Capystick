package com.capystick.designsystem.components

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricAuthenticator(
    private val activity: FragmentActivity,
    private val launchKeyguard: (Intent, () -> Unit, () -> Unit) -> Unit,
) {
    fun isDeviceSecure(): Boolean {
        val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    @Suppress("DEPRECATION")
    fun authenticate(
        title: String = "Autenticacion requerida",
        subtitle: String = "Usa tu huella o PIN para continuar",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit = {},
    ) {
        if (!isDeviceSecure()) {
            showMissingCredentialMessage(onError)
            return
        }

        val authenticators = if (Build.VERSION.SDK_INT >= 30) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        }

        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)

        if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
            canAuthenticate == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
        ) {
            val keyguardManager = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(title, subtitle)
            if (intent != null) {
                launchKeyguard(intent, onSuccess) { onError("Cancelado") }
                return
            }
        } else if (canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            showMissingCredentialMessage(onError)
            return
        } else if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onError("No se pudo iniciar la autenticacion")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showMissingCredentialMessage(onError: (String) -> Unit) {
        val message = "Configura un PIN, patron o contrasena para proteger notas"
        onError(message)
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun rememberBiometricAuthenticator(): BiometricAuthenticator {
    val context = LocalContext.current
    val currentOnSuccess = remember { mutableStateOf<(() -> Unit)?>(null) }
    val currentOnError = remember { mutableStateOf<(() -> Unit)?>(null) }

    val keyguardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentOnSuccess.value?.invoke()
        } else {
            currentOnError.value?.invoke()
        }
        currentOnSuccess.value = null
        currentOnError.value = null
    }

    return remember(context, keyguardLauncher) {
        BiometricAuthenticator(context as FragmentActivity) { intent, onSuccess, onError ->
            currentOnSuccess.value = onSuccess
            currentOnError.value = onError
            keyguardLauncher.launch(intent)
        }
    }
}
