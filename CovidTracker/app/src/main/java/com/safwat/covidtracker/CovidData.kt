package com.safwat.covidtracker

import com.google.gson.annotations.SerializedName
import java.util.*

data class CovidData(
    @SerializedName("date") val dateChecked: Date,

    @SerializedName("positive") val positiveIncrease: Int,

    @SerializedName("negative") val negativeIncrease: Int,

    @SerializedName("death") val deathIncrease: Int,

    @SerializedName("state") val states: String,

    )