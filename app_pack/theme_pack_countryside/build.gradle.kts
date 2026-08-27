plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("countryside")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
