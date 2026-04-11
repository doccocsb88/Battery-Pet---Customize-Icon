plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_120_dark")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
