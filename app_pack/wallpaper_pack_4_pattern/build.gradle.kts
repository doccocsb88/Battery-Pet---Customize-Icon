plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_4_pattern")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
