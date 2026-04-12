plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("wallpaper_jellyfish")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
