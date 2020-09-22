package com.safwat.covidtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
private const val BASE_URL="https://api.covidtracking.com/v1/"
private const val TAG="MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val gson=GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit= Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidServices=retrofit.create(CovidService::class.java)
        /***Fetch the National data***/
        covidServices.getNationalData().enqueue(object:Callback<List<CovidData>>{
            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
               Log.i(TAG,"OnResponse$response")
               val nationalResponse= response.body()
               if(nationalResponse==null){
                   Log.w(TAG,"Did not receive a Valid response body")
                   return
               }
               //Data is Valid
               nationalDailyData =nationalResponse.reversed()
               Log.i(TAG,"Update graph with "+nationalDailyData)

            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e( TAG, "OnFailure$t")
                Toast.makeText(applicationContext,"Error fetching National Data",Toast.LENGTH_SHORT).show()
            }

        })

        /***Fetch the States data***/
        covidServices.getStatesData().enqueue(object:Callback<List<CovidData>>{
            override fun onResponse(call: Call<List<CovidData>>, response: Response<List<CovidData>>) {
                Log.i(TAG,"OnResponse$response")
                val stateResponse= response.body()
                if(stateResponse==null){
                    Log.w(TAG,"Did not receive a Valid response body")
                    return
                }
                 perStateDailyData=stateResponse.reversed().groupBy {it.states}
                Log.i(TAG,"Update spinner with states name "+perStateDailyData)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e( TAG, "OnFailure$t")
                Toast.makeText(applicationContext,"Error fetching National Data",Toast.LENGTH_SHORT).show()
            }
        })
    }
}