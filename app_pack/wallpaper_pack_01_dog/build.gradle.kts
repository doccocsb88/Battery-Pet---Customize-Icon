plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_dog")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
