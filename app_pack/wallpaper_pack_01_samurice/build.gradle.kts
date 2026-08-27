plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_samurai")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
