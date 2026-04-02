package dev.hai.emojibattery.app

object AppStoreConfig {
    const val APP_ID: String = "co.q7labs.co.emoji"

    enum class PlayStoreLink(val url: String) {
        Web("https://play.google.com/store/apps/details?id=$APP_ID"),
        Market("market://details?id=$APP_ID"),
    }

    val playStoreWebUrl: String = PlayStoreLink.Web.url
    val playStoreMarketUrl: String = PlayStoreLink.Market.url
}
