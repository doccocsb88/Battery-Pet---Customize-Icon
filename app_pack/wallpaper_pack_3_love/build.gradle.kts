plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_3_love")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
