plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_2_cute")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
