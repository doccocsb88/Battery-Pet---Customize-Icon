plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("theme_options_pack")
    dynamicDelivery {
        deliveryType.set("install-time")
    }
}
