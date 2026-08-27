plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("sticker_pack_04")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
