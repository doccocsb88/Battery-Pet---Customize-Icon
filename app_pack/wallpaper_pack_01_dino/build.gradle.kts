plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_dino")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
