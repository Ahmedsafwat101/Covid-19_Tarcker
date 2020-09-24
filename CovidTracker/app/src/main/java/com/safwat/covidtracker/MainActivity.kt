package com.safwat.covidtracker

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.robinhood.ticker.TickerUtils
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

private const val BASE_URL = "https://api.covidtracking.com/v1/"
private const val TAG = "MainActivity"
private const val ALL_STATES = "All regions"

class MainActivity : AppCompatActivity() {

    private lateinit var currentlyShownData: List<CovidData>
    private lateinit var adapter: CovidSparkAdapter
    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gson = GsonBuilder(). setDateFormat("yyyy-MM-dd'T'HH:mm:ss.S")
            .create()
        Log.i(TAG,"Gson$gson")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidServices = retrofit.create(CovidService::class.java)

        /***Fetch the National data***/
        covidServices.getNationalData().enqueue(object : Callback<List<CovidData>> {
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.i(TAG, "OnResponse$response")
                val nationalResponse = response.body()
                if (nationalResponse.isNullOrEmpty()) {
                    Log.w(TAG, "Did not receive a Valid response body")
                    return
                }
                //Data is Valid
                setupEventListener()
                nationalDailyData = nationalResponse.reversed()
                Log.i(TAG, "Update graph with " + nationalDailyData)
                updateDisplayWithData(nationalDailyData)
            }
            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "OnFailure$t")

            }

        })

                                        /***Fetch the States data***/
        covidServices.getStatesData().enqueue(object : Callback<List<CovidData>> {
            override fun onResponse(
                call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG, "OnResponse$response")
                val stateResponse = response.body()
                if (stateResponse.isNullOrEmpty() ) {
                    Log.w(TAG, "Did not receive a Valid response body")
                    return
                }
                perStateDailyData = stateResponse.reversed().groupBy { it.states }
                Log.i(TAG, "Update spinner with states name ")
                updateSpinierData(perStateDailyData.keys)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "OnFailure $t")
            }
        })
    }

    private fun updateSpinierData(stateNames: Set<String>) {
        val stateList = stateNames.toMutableList()
        stateList.sort()
        stateList.add(0, ALL_STATES)

        //Add List to our spinner
        DataSetSpinnner.attachDataSource(stateList)
        DataSetSpinnner.setOnSpinnerItemSelectedListener { parent, view, position, id ->
            val selectedState = parent.getItemAtPosition(position) as String
            val selectedData = perStateDailyData[selectedState] ?: nationalDailyData
            updateDisplayWithData(selectedData)
        }
        //Add Spinner
    }

    private fun setupEventListener() {

        TikerNumber.setCharacterLists(TickerUtils.provideNumberList())
        //Add Listener for Scrubbing on the SparkView
        SparkView.isScrubEnabled = true
        SparkView.setScrubListener { value ->
            if (value is CovidData) {
                updateInfoData(value)
            }
        }
        //Response to selected buttons
        radioGroupTimeSlection.setOnCheckedChangeListener { radioGroup, checkedItem ->
            adapter.daysAgo = when (checkedItem) {
                R.id.BtWeek -> TimeScale.WEEK
                R.id.BtMonth -> TimeScale.MONTH
                else -> TimeScale.MAX
            }
            adapter.notifyDataSetChanged()
        }

        radioGroupMetric.setOnCheckedChangeListener { radioGroup, checkedItem ->
            when (checkedItem) {
                R.id.BtPositive -> updateDisplayMatrix(Metric.POSITIVE)
                R.id.BtNegtaive -> updateDisplayMatrix(Metric.NEGATIVE)
                else -> updateDisplayMatrix(Metric.DEATH)
            }
        }

    }

    private fun updateDisplayMatrix(metric: Metric) {
        //Update colors
        val color = when (metric) {
            Metric.POSITIVE -> R.color.colorPositive
            Metric.NEGATIVE -> R.color.colorNegative
            Metric.DEATH -> R.color.colorDeath
        }
        val colorInt = ContextCompat.getColor(this, color)
        SparkView.lineColor = colorInt
        TikerNumber.setTextColor(colorInt)
        adapter.metric = metric
        adapter.notifyDataSetChanged()

        //Reset the value that shown on the bottom text View
        updateInfoData(currentlyShownData.last())
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        currentlyShownData = dailyData
        //Create a new  SparkAdapter with data
        adapter = CovidSparkAdapter(dailyData)
        SparkView.adapter = adapter
        // Update radio buttons to select the positive cases and max time by default
        BtPositive.isChecked = true
        BtMax.isChecked = true
        // Display number with the most recent data
        updateDisplayMatrix(Metric.POSITIVE)
    }

    private fun updateInfoData(recentCovidData: CovidData) {
        //Number of Covid-19
        val numCases = when (adapter.metric) {
            Metric.NEGATIVE -> recentCovidData.negativeIncrease
            Metric.POSITIVE -> recentCovidData.positiveIncrease
            Metric.DEATH -> recentCovidData.deathIncrease
        }
        TikerNumber.text = numCases.toString()
        val outputDateFormat = SimpleDateFormat("MMMM dd, yyyy ", Locale.US)
        TvDate.text = outputDateFormat.format(recentCovidData.dateChecked)


    }
}