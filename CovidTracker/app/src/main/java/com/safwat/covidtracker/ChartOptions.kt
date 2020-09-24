package com.safwat.covidtracker

enum class Metric{
    NEGATIVE,POSITIVE,DEATH
}
enum class TimeScale(val numOfDays:Int){
    WEEK(10),
    MONTH(30),
    MAX(-1)
}