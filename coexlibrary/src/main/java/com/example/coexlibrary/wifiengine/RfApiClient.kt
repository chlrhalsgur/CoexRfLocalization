package com.example.mylibrary

import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


//private const val SERVER_URL = "http://49.169.175.135:5000/rf/"
private const val SERVER_URL = "http://163.152.52.241:5000/rf/"

class RfApiClient {
    suspend fun serverRequestRfSimilarity(
        data: String,
        route: String = "get_rf_similarity/"
    ): MutableMap<String, Any> = withContext(
        Dispatchers.IO) {
        val httpClient: HttpClient = DefaultHttpClient()
        var httpPost = HttpPost(SERVER_URL)

        try {
            val stringEntity = StringEntity(data)
            stringEntity.setContentType("application/json")
            httpPost = HttpPost(SERVER_URL + route)
            httpPost.entity = stringEntity

            val response = httpClient.execute(httpPost)
            val entity: HttpEntity = response.entity


            // 응답 데이터 읽기
            val inputStream = entity.content
            val reader = BufferedReader(InputStreamReader(inputStream))

            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }


            val responseString = responseStringBuilder.toString()
            val jsonResponse = JSONObject(responseString)

            val status = jsonResponse.optString("status").toInt()
//            val rfLocalizationResult = jsonResponse.optString("rf_localization_result")
            Log.d("network", "request failed dfdf")

            val rfLocalizationResult = jsonArrayToDoubleArrayList(jsonResponse.getJSONArray("rf_localization_result"))
            val weightAverage = jsonArrayToDoubleArrayList(jsonResponse.getJSONArray("weighted_average"))
//            val rfLocalizationResult = jsonArrayToArrayListOfArrayList(jsonResponse.getJSONArray("rf_localization_result"))
            return@withContext mutableMapOf("status" to status, "rf_localization_result" to rfLocalizationResult, "weighted_average" to weightAverage)

        } catch (e: Exception) {
            Log.d("network", "request failed $e")
            e.printStackTrace()
            return@withContext mutableMapOf("status" to 401, "rf_localization_result" to "", "weighted_average" to "")
        } finally {
            httpClient.connectionManager.shutdown()
        }
    }

    // 서버로 ssid 리스트를 보내고 부분 데이터 받아와서 로컬에서 처리
    suspend fun serverRequestPartialDataSSID(
        data: String,
        route: String = "get_partial_data/"
    ): Map<String, Any> = withContext(
        Dispatchers.IO) {
        val httpClient: HttpClient = DefaultHttpClient()
        var httpPost = HttpPost(SERVER_URL)

        try {
            val stringEntity = StringEntity(data)
            stringEntity.setContentType("application/json")
            httpPost = HttpPost(SERVER_URL + route)
            httpPost.entity = stringEntity

            val response = httpClient.execute(httpPost)

            val entity: HttpEntity = response.entity


            // 응답 데이터 읽기
            val inputStream = entity.content
            val reader = BufferedReader(InputStreamReader(inputStream))

            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }


            val responseString = responseStringBuilder.toString()
            val jsonResponse = JSONObject(responseString)

            val status = jsonResponse.optString("status")
            val mapData = jsonResponse.optString("map_data")

            val gson = Gson()
            val type = object : TypeToken<MutableMap<String, Any>>() {}.type
            val mutableMap: MutableMap<String, MutableMap<String, ArrayList<ArrayList<Int>>>> = gson.fromJson(mapData, type)
            return@withContext mutableMapOf("status" to status, "mapData" to mutableMap)

        } catch (e: Exception) {
            Log.d("network", "request failed $e")
            e.printStackTrace()
            return@withContext mutableMapOf("status" to 401, "mapData" to "")
        } finally {
            httpClient.connectionManager.shutdown()
        }
    }

    suspend fun serverRequestPartialDataJson(
        data: String,
        route: String = "get_partial_data/"
    ): Map<String, Any> = withContext(
        Dispatchers.IO) {
//    fun serverRequestPartialData(data: String, route: String = "get_partial_data/"): Map<String, Any> {
        val httpClient: HttpClient = DefaultHttpClient()
        var httpPost = HttpPost(SERVER_URL)

        try {
            val stringEntity = StringEntity(data)
            stringEntity.setContentType("application/json")
            httpPost = HttpPost(SERVER_URL + route)
            httpPost.entity = stringEntity

            val response = httpClient.execute(httpPost)

            val entity: HttpEntity = response.entity


            // 응답 데이터 읽기
            val inputStream = entity.content
            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }
            responseStringBuilder.toString()


            val responseString = responseStringBuilder.toString()
            val jsonResponse = JSONObject(responseString)

            val status = jsonResponse.optString("status")
            val mapData = jsonResponse.optString("map_data")

            val gson = Gson()
            val type = object : TypeToken<MutableMap<String, Any>>() {}.type
            val mutableMap: MutableMap<String, MutableMap<String, ArrayList<ArrayList<Int>>>> = gson.fromJson(mapData, type)

            return@withContext mutableMapOf("status" to status, "mapData" to mutableMap)

        } catch (e: Exception) {
            Log.d("network", "request failed $e")
            e.printStackTrace()
            return@withContext mutableMapOf("status" to 401, "mapData" to "")
        } finally {
            httpClient.connectionManager.shutdown()
        }
    }
    fun serverRequestFloor(data: String, route: String = "get_floor/"): Map<String, Any> {
        val httpClient: HttpClient = DefaultHttpClient()
        var httpPost = HttpPost(SERVER_URL)
        try {
            val stringEntity = StringEntity(data)
            stringEntity.setContentType("application/json")
            httpPost = HttpPost(SERVER_URL + route)
            httpPost.entity = stringEntity

            val response = httpClient.execute(httpPost)

            val entity: HttpEntity = response.entity


            // 응답 데이터 읽기
            val inputStream = entity.content
            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }
            responseStringBuilder.toString()

            val responseData = Json.decodeFromString<ResponseDataFloor>(responseStringBuilder.toString())


            return hashMapOf("status" to responseData.status, "floor" to responseData.floor)

        } catch (e: Exception) {
            Log.d("network", "request failed $e")
            e.printStackTrace()
            return hashMapOf("status" to 401, "floor" to "")
        } finally {
            httpClient.connectionManager.shutdown()
        }
    }
    fun serverRequestRange(data: String, route: String = "get_range/"): Map<String, Any> {
        val httpClient: HttpClient = DefaultHttpClient()
        var httpPost = HttpPost(SERVER_URL)
        try {
            val stringEntity = StringEntity(data)
            stringEntity.setContentType("application/json")
            httpPost = HttpPost(SERVER_URL + route)
            httpPost.entity = stringEntity

            val response = httpClient.execute(httpPost)

            val entity: HttpEntity = response.entity

            // 응답 데이터 읽기
            val inputStream = entity.content
            val reader = BufferedReader(InputStreamReader(inputStream))
            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }

            val responseData = Json.decodeFromString<ResponseDataRange>(responseStringBuilder.toString())


            return hashMapOf("status" to responseData.status, "range" to responseData.range)

        } catch (e: Exception) {
            Log.d("network", "request failed $e")
            e.printStackTrace()
            return hashMapOf("status" to 401, "range" to arrayListOf(0.0,0.0,0.0,0.0))
        } finally {
            httpClient.connectionManager.shutdown()
        }
    }
    fun jsonArrayToDoubleArrayList(jsonArray: JSONArray): ArrayList<Double> {
        return ArrayList<Double>().apply {
            for (i in 0 until jsonArray.length()) {
                add(jsonArray.getInt(i).toDouble())
            }
        }
    }
    fun jsonArrayToArrayListOfArrayList(jsonArray: JSONArray): ArrayList<ArrayList<Double>> {
        val arrayList = ArrayList<ArrayList<Double>>()
        for (i in 0 until jsonArray.length()) {
            val innerJsonArray = jsonArray.getJSONArray(i)
            val innerArrayList = ArrayList<Double>()
            for (j in 0 until innerJsonArray.length()) {
                innerArrayList.add(innerJsonArray.getInt(j).toDouble())
            }
            arrayList.add(innerArrayList)
        }
        return arrayList
    }
}

@Serializable
data class WifiScanData(val data: String)


@Serializable
data class ResponseDataInit(
    val status: Int,
    @SerialName("range")
    val range: String
)
@Serializable
data class ResponseDataFloor(
    val status: Int,
    @SerialName("floor")
    val floor: String
)
@Serializable
data class ResponseDataRange(
    val status: Int,
    @SerialName("range")
    val range: ArrayList<Double>
)

