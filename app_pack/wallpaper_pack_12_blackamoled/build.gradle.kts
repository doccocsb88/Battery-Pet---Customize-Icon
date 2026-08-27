plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_12_blackamoled")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
