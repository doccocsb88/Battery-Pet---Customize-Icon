plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_19_urf_kate")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
