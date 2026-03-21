package dev.hai.emojibattery.data.volio

import com.google.gson.annotations.SerializedName

data class VolioListResponse<T>(
    @SerializedName("data") val data: List<T>?,
    @SerializedName("message") val message: String?,
    @SerializedName("status") val status: Int?,
)

data class VolioCategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("status") val status: Boolean?,
)

data class VolioItemCustomFieldsDto(
    @SerializedName("content") val content: String?,
)

data class VolioEmojiBatteryItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("photo") val photo: String? = null,
    @SerializedName("is_pro") val isPro: Boolean? = null,
    @SerializedName("custom_fields") val customFields: VolioItemCustomFieldsDto? = null,
)
