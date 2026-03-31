plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("sticker_pack_07")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
