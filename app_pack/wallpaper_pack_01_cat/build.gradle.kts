plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_cat")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
