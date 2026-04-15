plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_japans")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
