plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_132_madeinblue")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
