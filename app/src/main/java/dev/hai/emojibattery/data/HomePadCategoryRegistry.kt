package dev.hai.emojibattery.data

object HomePadCategoryRegistry {
    private val bundledCategoryIds: Set<String> = setOf(
        "e2eca2b6-23ae-51bf-9eaf-aaef1a3b5312", // Ocean
        "087e2fe0-b2ae-55c1-9e91-66f7c20d7d7c", // Vietnam
        "7716667c-abd4-58f8-b417-67416fafd9b0", // Korean
        "92c365fd-5815-5504-adc7-47f97e366a9c", // Japan
        "b36a3856-c48a-5a6b-85d6-1519dd60af35", // Winter
        "d3cbb799-a633-50fa-a509-3396beee6069", // China
    )

    val categoryIds: Set<String> = setOf(
        "09197190-2899-4035-802f-766a8bfa3273",
        "701a7eb1-bea4-4865-9e77-388f8b6161fa",
        "224434da-74fc-4156-8741-296c1da4434d",
        "78daf738-4e07-4688-aea4-c7323f89e46e",
        "52826fbd-9f8a-4bb9-8db9-e4031eb056bb",
        "ef818e6b-b015-4539-8116-679ac99b8e2b",
        "32d680b7-e2de-4d0d-bcd9-7e6610667115",
        "fd34c543-6301-4293-99d5-e7f42180da3d",
        "2f5fb2ed-dc67-4a91-9527-d9825596db59",
        "25564f24-07bc-4a68-aaa7-d3643e7013e4",
        "ccc1b13c-428e-482b-8099-2cea15d9c0ed",
        "4dcf0fb0-819a-43d0-8bea-8b809e5e149f",
        "51b37341-f450-4000-ae62-c98787fdfefe",
        "08d01cfc-6648-4204-9005-14a8e022ca64",
        "6a7d9b55-8d0b-4a2a-959d-e00e019b7924",
        "e2e2b933-447f-414b-b7cc-19b40a20a53b",
        "2e827af4-6810-4fae-a3f4-161127879610",
        "904a6b2f-3641-492b-9dee-d41a47c473de",
        "04cf9760-e5b8-4098-b373-b7826304b755",
        "df41ad61-c83b-4301-a87d-8861df7bec44",
        "68e6df58-8088-459c-9eb4-29cfe7e71b2a",
        "eda6c9ca-4b1e-499b-90ea-919241996945",
    )

    fun hasPadPack(categoryId: String): Boolean = categoryIds.contains(categoryId)

    fun usesBundledAssets(categoryId: String): Boolean = bundledCategoryIds.contains(categoryId)
}
