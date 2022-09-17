package com.raafat.revise.data


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Aya(
    @SerializedName("aya_no")
    val ayaNo: Int,
    @SerializedName("aya_text")
    val ayaText: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("sura_no")
    val sora: Int
)