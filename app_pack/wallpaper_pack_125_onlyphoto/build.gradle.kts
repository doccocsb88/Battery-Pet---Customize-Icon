plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_125_onlyphoto")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
