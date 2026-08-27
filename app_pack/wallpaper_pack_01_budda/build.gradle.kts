plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_budda")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
