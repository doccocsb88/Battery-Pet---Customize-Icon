plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_107_vaporwave")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
