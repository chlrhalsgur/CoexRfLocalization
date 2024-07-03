package com.example.CoexRfLocalization

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.net.SocketException


private const val userPointURL = "http://163.152.52.60:8001/coex/"
private var toast: Toast? = null



class DashBoard {

/*
    object HttpClientFactory {
        val client: HttpClient by lazy {
            HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer(Json {
                        ignoreUnknownKeys = true
                    })
                }
            }
        }
    }


    suspend fun sendDataPoint(requestData: RequestDataPoint): ResponseDataArea {
        val response: HttpResponse = HttpClientFactory.client.post(userPointURL) {
            contentType(io.ktor.http.ContentType.Application.Json)
            body = requestData
        }

        return response.receive()
    }
*/


    companion object {
        val client: HttpClient by lazy {
            HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
        }
    }
/*
    suspend fun sendDataPoint2(requestData: RequestDataPoint): ResponseDataArea {
        val response: HttpResponse = client.post("${userPointURL}user_point/") {
            contentType(io.ktor.http.ContentType.Application.Json)
            body = requestData
        }

        return response.receive()
    }
*/

    suspend fun sendDataPoint(requestData: RequestDataPoint): ResponseDataArea? {
        var attempt = 0
        while (attempt < 3) {
            try {
                val response: HttpResponse = client.post("${userPointURL}user_point/") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    body = requestData
                }
                // 응답이 성공적이면 receive()를 호출하고 반환
                Log.d("asdjasdkjasdjk", "ss")
                return response.receive()
            } catch (e: SocketException) {
                Log.e("sendDataPoint error", "SocketException: ${e.message}")
                attempt++
                delay(333)  // 1초 대기 후 재시도
            } catch (e: Exception) {
                Log.e("sendDataPoint error", "Exception: ${e.message}")
                attempt++
                delay(333)  // 1초 대기 후 재시도
            }
        }
        // 재시도 후에도 실패하면 null 반환
        return null
    }





    suspend fun sendDataPointDelete(requestData: RequestDataPointDelete): HttpResponse {
        val response: HttpResponse = client.post("${userPointURL}user_point_del/") {
            contentType(io.ktor.http.ContentType.Application.Json)
            body = requestData
        }

        return response.receive()
    }


    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun areaMsg(type: String): String {
        return when(type) {
            "danger" -> "위험구역 입니다."
            else -> "area error"
        }
    }


    fun showToastArea(context: Context, msg: String) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast?.show()
    }



}



// 데이터 클래스 정의
@Serializable
data class RequestDataPoint(
    val user_seq : Int,
    val x : Int,
    val y : Int,
    val floor : Int
)


// 데이터 클래스 정의
@Serializable
data class RequestDataPointDelete(
    val user_seq : Int,
    val floor : Int
)

@Serializable
data class ResponseDataArea(
    val type : String,
    val order : Int
)