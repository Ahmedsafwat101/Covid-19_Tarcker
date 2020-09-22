package com.safwat.covidtracker

import com.google.gson.annotations.SerializedName
import java.util.*

data class CovidData(
    @SerializedName("dateChecked") val dateChecked: Date,

    @SerializedName("positiveIncrease") val positiveIncrease: String,

    @SerializedName("negativeIncrease") val negativeIncrease: String,

    @SerializedName("deathIncrease") val deathIncrease: String,

    @SerializedName("states") val states: String,

    )