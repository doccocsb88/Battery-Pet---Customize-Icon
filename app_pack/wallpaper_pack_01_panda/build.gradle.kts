plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_01_panda")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
