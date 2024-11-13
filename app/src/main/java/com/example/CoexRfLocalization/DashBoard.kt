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


private const val userPointURL = "http://163.152.52.60:8001/"
private var toast: Toast? = null



class DashBoard {

    companion object {
        val client: HttpClient by lazy {
            HttpClient {
                install(JsonFeature) {
                    serializer = KotlinxSerializer()
                }
            }
        }
    }


    suspend fun sendDataPoints(requestData: RequestDataPoint): ResponseDataArea? {
        var attempt = 0
        while (attempt < 3) {
            try {
                val response: HttpResponse = client.post("${userPointURL}/db/user_coordinate") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    body = requestData
                }
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

    suspend fun sendDataPoint(requestData: RequestDataPoint): ResponseDataArea? {
        var attempt = 0
        while (attempt < 3) {
            try {
                val response: HttpResponse = client.post("${userPointURL}/db/user_coordinate") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    body = requestData
                    Log.d("asdad", body.toString())
                }
                return response.receive()  // 응답이 성공적이면 데이터를 반환
            } catch (e: Exception) {
                Log.e("sendDataPoint error", "Exception: ${e.message}")
                attempt++
                delay(333)  // 333ms 대기 후 재시도
            }
        }
        return null
    }



}



// 데이터 클래스 정의
@Serializable
data class RequestDataPoint(
    val user_id : String,
    val pos_x : Int,
    val pos_y : Int,
    val pos_z : Int,
    val recorded_at : String
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