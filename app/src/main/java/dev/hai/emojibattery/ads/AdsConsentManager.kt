package dev.hai.emojibattery.ads

import android.app.Activity
import android.content.Context
import android.util.Log
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
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
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
