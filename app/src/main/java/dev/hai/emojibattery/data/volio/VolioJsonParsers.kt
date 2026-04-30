package dev.hai.emojibattery.data.volio

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

fun parseVolioCategories(json: String): List<VolioCategoryDto> {
    val data = parseDataArray(json) ?: return emptyList()
    return data.mapNotNull { element ->
        val obj = element.asJsonObjectOrNull() ?: return@mapNotNull null
        val id = obj.string("id") ?: return@mapNotNull null
        VolioCategoryDto(
            id = id,
            name = obj.string("name"),
            status = obj.boolean("status"),
        )
    }
}

fun parseVolioItems(json: String): List<VolioEmojiBatteryItemDto> {
    val data = parseDataArray(json) ?: return emptyList()
    return data.mapNotNull { element ->
        val obj = element.asJsonObjectOrNull() ?: return@mapNotNull null
        val id = obj.string("id") ?: return@mapNotNull null
        val customFieldsObj = obj.obj("custom_fields")
        VolioEmojiBatteryItemDto(
            id = id,
            categoryId = obj.string("category_id"),
            name = obj.string("name"),
            thumbnail = obj.string("thumbnail"),
            photo = obj.string("photo"),
            isPro = obj.boolean("is_pro"),
            customFields = customFieldsObj?.let { fields ->
                VolioItemCustomFieldsDto(
                    content = fields.string("content"),
                    contentZip = fields.string("contentZip"),
                    battery = fields.string("battery"),
                    emoji = fields.string("emoji"),
                )
            },
        )
    }
}

private fun parseDataArray(json: String): JsonArray? {
    val root = runCatching { JsonParser.parseString(json).asJsonObject }.getOrNull() ?: return null
    return root.getAsJsonArray("data")
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? =
    if (isJsonObject) asJsonObject else null

private fun JsonObject.string(key: String): String? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asString }.getOrNull()
}

private fun JsonObject.boolean(key: String): Boolean? {
    val value = get(key) ?: return null
    if (value.isJsonNull) return null
    return runCatching { value.asBoolean }.getOrNull()
}

private fun JsonObject.obj(key: String): JsonObject? {
    val value = get(key) ?: return null
    if (!value.isJsonObject) return null
    return value.asJsonObject
}
