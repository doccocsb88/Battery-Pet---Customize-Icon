plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("floralgarden")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
