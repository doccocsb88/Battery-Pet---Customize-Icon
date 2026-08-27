plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("sticker_pack_09")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
