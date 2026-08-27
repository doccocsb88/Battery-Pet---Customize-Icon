plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("sticker_pack_08")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
