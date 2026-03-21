package dev.hai.emojibattery.data.volio

import retrofit2.http.GET
import retrofit2.http.Query

interface VolioStoreApi {

    @GET("categories/all")
    suspend fun categoriesAll(
        @Query("parent_id") parentId: String,
    ): VolioListResponse<VolioCategoryDto>

    @GET("items")
    suspend fun items(
        @Query("category_id") categoryId: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): VolioListResponse<VolioEmojiBatteryItemDto>
}
