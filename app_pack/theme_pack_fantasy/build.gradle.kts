plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("fantasy")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
