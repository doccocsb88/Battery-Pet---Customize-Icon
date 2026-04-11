plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_21_geometric")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
