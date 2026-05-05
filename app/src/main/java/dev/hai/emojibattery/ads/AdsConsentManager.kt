package dev.hai.emojibattery.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import co.q7labs.co.emoji.BuildConfig
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class AdsConsentManager(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(appContext)

    @Volatile
    private var isConsentFlowInProgress = false

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun requestConsentInfo(
        activity: Activity,
        onComplete: () -> Unit = {},
    ) {
        if (isConsentFlowInProgress) {
            Log.d(TAG, "requestConsentInfo: already running")
            return
        }
        isConsentFlowInProgress = true
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
        if (BuildConfig.DEBUG) {
            paramsBuilder.setConsentDebugSettings(
                ConsentDebugSettings.Builder(appContext)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .build(),
            )
        }
        consentInformation.requestConsentInfoUpdate(
            activity,
            paramsBuilder.build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(
                            TAG,
                            "loadAndShowConsentFormIfRequired: code=${formError.errorCode} msg=${formError.message}",
                        )
                    }
                    isConsentFlowInProgress = false
                    onComplete()
                }
            },
            { requestError ->
                Log.w(
                    TAG,
                    "requestConsentInfoUpdate: code=${requestError.errorCode} msg=${requestError.message}",
                )
                isConsentFlowInProgress = false
                onComplete()
            },
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (FormError?) -> Unit = {},
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w(
                    TAG,
                    "showPrivacyOptionsForm: code=${formError.errorCode} msg=${formError.message}",
                )
            }
            onDismissed(formError)
        }
    }

    companion object {
        private const val TAG = "AdsConsentManager"
    }
}
