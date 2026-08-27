plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_112_nature")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
