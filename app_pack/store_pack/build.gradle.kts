plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("store_pack")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
