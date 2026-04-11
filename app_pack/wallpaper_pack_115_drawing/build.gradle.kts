plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_115_drawing")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
