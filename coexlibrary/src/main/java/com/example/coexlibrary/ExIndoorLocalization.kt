package com.example.coexlibrary

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import com.example.coexlibrary.sensors.MovingAverage
import com.example.heropdr.HeroPDR
import com.example.heropdr.PDR
import com.example.mylibrary.RFLocalization
import org.json.JSONObject
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private val TESTBED ="coex"
class ExIndoorLocalization(context: Context) {
    private var coor = arrayListOf(0.0,0.0,0.0)
    var coor2 = arrayListOf(0.0,0.0,0.0)
    var outLiered = arrayListOf(0.0,0.0,0.0)
    /* 자이로 관련 */
    private var gyroCaliValue = 0.0f
    var sampledYawAngle: Float = 0.0f
    var sampledYawAngle2: Float = 0.0f
    var compassDir: Float = 0.0f
    var dirOffset = -1.0
    private var gyroStableCount : Int = 100

    /* PDR 관련 */   // 230911  변수 몇개 추가
    private val heroPDR: HeroPDR = HeroPDR()
    private lateinit var pdrResult: PDR
    var userDir: Float = 0F
    private var stepLength: Float = 0.0f
    private var gravMatrix = FloatArray(3)
    private var nowPosition: Array<Float> = arrayOf(0F, 0F, 2F)
    var stepCount: Int = 0


    /* 휠체어 관련 */
    private val accYMovingAverage: MovingAverage = MovingAverage(10)
    private val accZMovingAverage: MovingAverage = MovingAverage(10)

    /* 센서 관련 */
    var isSensorStabled: Boolean = false
    private var magMatrix = FloatArray(3)
    private var accMatrix = FloatArray(3)

    var curFloor: Int = -1

    /* WiFi 엔진 관련 */
    var wifipermitted = false
    var wifiRange = arrayListOf(-1.0f, -1.0f, -1.0f, -1.0f)
    private var rfLocalization: RFLocalization
    var matchingCoor = arrayListOf(0.0, 0.0, 0.0)

    /* Instant Localization 관련 */
    private var answer_angle: Float = -1.0f  // 230918 원준 : 문장 추가
    var isStable = false // 230920
    var coordArrayEMA: ArrayList<ArrayList<Double>>

    private var cntxt: Context
    val gpsMapResourceID = context.resources.getIdentifier("indoor_map", "raw", context.packageName)
    private var mapMatchingData = loadJsonFromRaw(context, gpsMapResourceID)
    init {
        coordArrayEMA = arrayListOf()

        cntxt = context
        rfLocalization = RFLocalization(context, TESTBED, mapMatchingData)

//        rfLocalization.rfModuleInit()

        nowPosition[2] = curFloor.toFloat()
    }
    private fun isReadyLocalization(event: SensorEvent, fusedOrientation : FloatArray) : Boolean {
        if (isSensorStabled) {
            return true
        } else {
            when (event.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {  // 230911 메인 엑티비티에 있던 자기장, 자이로 안정화 코드 내부로 이동
                    gyroStableCount--
                }
            }
            if (gyroStableCount <= 0 ) {
                gyroCaliValue = ((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360).toFloat()
            }

            isSensorStabled = gyroStableCount <= 0
            return isSensorStabled
        }
    }

    fun sensorChanged(event: SensorEvent?, fusedOrientation: FloatArray): ArrayList<Double> {
        if ((event ?: false).let { isReadyLocalization(event!!, fusedOrientation) }) {
            userDir = rfLocalization.getRfDirection(fusedOrientation, gyroCaliValue, (((Math.toDegrees(fusedOrientation[0].toDouble()).toFloat() + 360) % 360 - gyroCaliValue ) + 360) % 360)

            sampledYawAngle = (((Math.toDegrees(fusedOrientation[0].toDouble()).toFloat() + 360) % 360 - gyroCaliValue + answer_angle) + 360) % 360 // 230918 원준 : 문장수정 (answer_angle 추가)

//            userDir = smoothing(userDir)

            when (event!!.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accMatrix = event.values.clone()
                }

                Sensor.TYPE_LIGHT -> {
                    heroPDR.setLight(event.values[0])
                }

                Sensor.TYPE_ROTATION_VECTOR -> {
                    heroPDR.setQuaternion(event.values.clone())
                }

                Sensor.TYPE_PRESSURE -> {
                    heroPDR.setPressure(event.values.clone()[0])
                }

                Sensor.TYPE_GRAVITY -> {
                    gravMatrix = event.values.clone()
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magMatrix = event.values.clone()
                }

                Sensor.TYPE_GYROSCOPE -> {
                    heroPDR.setDirection(
                        ((Math.toDegrees(fusedOrientation[1].toDouble()) + 360) % 360),
                        ((Math.toDegrees(fusedOrientation[2].toDouble()) + 360) % 360),
                        ((Math.toDegrees(fusedOrientation[0].toDouble()) + 360) % 360),
                        gravMatrix[1], gravMatrix[2]
                    )
                }

                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    accYMovingAverage.newData(event.values[1].toDouble())
                    accZMovingAverage.newData(event.values[2].toDouble())

                    if (heroPDR.isStep(event.values.clone(), accMatrix)) {
                        pdrResult = heroPDR.getStatus()
                        stepCount = pdrResult.totalStepCount
                        stepLength = pdrResult.stepLength.toFloat() - 0.055f

                        val rfResult = rfLocalization.getRfLocalization(stepLength)
                        // pdr section
                        val rfStatusCode = rfResult["rfStatusCode"] as Double

                        coor2 = rfResult["rfFiltered"] as ArrayList<Double>
                        coor = if(199.0 < rfStatusCode && rfStatusCode < 300.0){
                            rfResult["rfCoor"] as ArrayList<Double>
                        }else{
                            arrayListOf(coor[0] - (sin(Math.toRadians(userDir.toDouble())) * stepLength*10),
                                coor[1] + (cos(Math.toRadians(userDir.toDouble())) * stepLength*10), coor[2])
                        }
                        rfLocalization.setNowCoor(coor)
                        matchingCoor = rfLocalization.mapMatching(coor)
                        coordArrayEMA = rfLocalization.coordArrayEMA
                        outLiered = rfResult["rfOutlier"] as ArrayList<Double>
                        Log.d("asdasdasd 22", rfStatusCode.toString())
                    }
                }
            }
        }
//        return coor   맵매칭 이전 좌표
        return matchingCoor
    }

    fun getDistance(pos1: Array<Float>, pos2: Array<Float>): Float { // 거리 차이 반환 (미터 단위)
        return sqrt((pos1[0] - pos2[0]).pow(2) + (pos1[1] - pos2[1]).pow(2)) * 0.1f
    }

    fun loadJsonFromRaw(context: Context, resourceId: Int): JSONObject {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        return JSONObject(jsonString)
    }
    // 주어진 좌표와 선분 사이의 최소 거리 및 해당 지점 계산


}