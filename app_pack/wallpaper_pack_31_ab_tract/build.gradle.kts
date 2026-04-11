plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_31_ab_tract")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
