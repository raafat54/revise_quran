package com.raafat.revise.data


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Aya(
    @SerializedName("aya_no")
    val ayaNo: Int,
    @SerializedName("aya_text")
    val ayaText: String,
    @SerializedName("aya_text_emlaey")
    val ayaTextEmlaey: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("jozz")
    val jozz: Int,
    @SerializedName("page")
    val page: String,
    @SerializedName("sura_no")
    val sora: Int,
    @SerializedName("sura_name_ar")
    val soraNameAr: String,
    @SerializedName("sura_name_en")
    val soraNameEn: String,
)