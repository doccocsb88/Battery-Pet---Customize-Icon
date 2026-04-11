plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_100_getmotivated")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
