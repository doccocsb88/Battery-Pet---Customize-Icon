plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("chinese_spring_landscape")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
