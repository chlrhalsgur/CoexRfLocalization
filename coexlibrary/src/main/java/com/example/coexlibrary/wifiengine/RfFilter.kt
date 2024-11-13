package com.example.mylibrary

import android.util.Log
import java.io.Serializable
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// 초기 수렴과 재수렴 기준 설정 필요. 지금은 일단 초기 값을 무조건 믿는 것으로 함.
class RfFilter(sequenceLength: Int = 10, outlierSize: Int = 300 ) {
    private var coordArray: ArrayList<ArrayList<Double>>
    var coordArrayEMA: ArrayList<ArrayList<Double>>
    private var seqLength: Int
    private var oFilteredCoor: ArrayList<Double>
    private var kFilteredCoor: ArrayList<Double>
    private lateinit var coorQueue: ArrayList<ArrayList<Double>>
    private var prevCoor: ArrayList<Double>
    private var outlierDiff: Int
    private var pdrCoor: ArrayList<Double>
    private var conversion: Int // 0: 초기, 1: 신뢰도 낮음, 2: 수렴
    var nowCoor = arrayListOf(0.0, 0.0)
    private var seq = 0
    private var statusCnt = 0
    private var seqLengthEMA = sequenceLength

    init {
        seqLength = sequenceLength
        coordArray = arrayListOf()
        coordArrayEMA = arrayListOf()
        oFilteredCoor = arrayListOf(0.0, 0.0)
        kFilteredCoor = arrayListOf(0.0, 0.0)
        prevCoor = arrayListOf(0.0, 0.0, 0.0)
        outlierDiff = outlierSize
        conversion = 0
        pdrCoor = arrayListOf(0.0, 0.0)
    }


    fun applyOutlierFilter(
        data: ArrayList<ArrayList<Double>>,
        kDistance: Double = 300.0
    ): ArrayList<Double> {
        if (data.size <= 1) return arrayListOf(-1.0, -1.0, -1.0)

        fun distance(p1: List<Double>, p2: List<Double>): Double {
            return sqrt((p1[0] - p2[0]).pow(2) + (p1[1] - p2[1]).pow(2))
        }

        val totalPoints = data.size
        val threshold = totalPoints / 2  // 좌표 개수의 50%

        val filteredData = data.filter { point ->
            val neighborCount = data.count { otherPoint ->
                if (point != otherPoint) {
                    val dist = distance(point, otherPoint)
                    dist <= kDistance
                } else {
                    false
                }
            }

            neighborCount >= threshold
        }

        return if (filteredData.isNotEmpty()) {
            val avgX = filteredData.map { it[0] }.average()
            val avgY = filteredData.map { it[1] }.average()
            val centerOfMass = listOf(avgX, avgY)

            val distancesToCenter = filteredData.map { point ->
                distance(centerOfMass, point)
            }
            val avgDistanceToCenter = distancesToCenter.average()

            Log.d("Average distance to", avgDistanceToCenter.toString())

            arrayListOf(avgX, avgY, avgDistanceToCenter)
        } else {
            arrayListOf(-1.0, -1.0, -1.0)
        }
    }





    private fun calculateEuclideanDistance(coords1: ArrayList<Double>, coords2: ArrayList<Double>): Double {
        return sqrt((coords1[0] - coords2[0]).pow(2) + (coords1[1] - coords2[1]).pow(2))
    }

    /**
     * outliers filter -> kalman filter
     */
    fun filterCoor(rfLocalizationCoor: ArrayList<Double>, statusCode: Double): Map<String, Serializable> {
        var filterStatusCode = 100.0
        var outputCoor = arrayListOf(0.0,0.0)

        if (nowCoor == arrayListOf(0.0, 0.0)){
            nowCoor = rfLocalizationCoor
        }
        when (statusCode){
            300.0 -> {
                filterStatusCode = 301.0
                outputCoor = rfLocalizationCoor
            }
            200.0 -> {
                coordArray.add(rfLocalizationCoor)
                seq++
                val distanceDiff = calculateEuclideanDistance(nowCoor, rfLocalizationCoor)

                if (distanceDiff > outlierDiff){ // 부적절 좌표
                    outputCoor = rfLocalizationCoor
                    filterStatusCode = 401.0
                }else{ // 적절한 좌표
                    outputCoor = rfLocalizationCoor
                    filterStatusCode = 201.0
                }
            }
            else ->{
                outputCoor = prevCoor
                filterStatusCode = 402.0
            }// 400
        }
        prevCoor // 마지막으로 들어온 적절 좌표
        return mapOf("statusCode" to filterStatusCode, "outputCoor" to outputCoor)
    }


    fun femaFilter(
        rfLocalizationCoor: ArrayList<Double>,
        statusCode: Double,
        dir: Double,
        stepLength: Float
    ): Map<String, Serializable> {
        var xAvg = 0.0
        var yAvg = 0.0
        if (199.0 < statusCode && statusCode < 300.0){
            statusCnt++
            coordArrayEMA.add(rfLocalizationCoor)
        }
//        for (coor in coordArrayEMA){
//            coor[0] = coor[0] - (sin(Math.toRadians(dir)) * stepLength*10)
//            coor[1] = coor[1] + (cos(Math.toRadians(dir)) * stepLength*10)
//        }
        coordArrayEMA.forEachIndexed { index, coor ->
            if (index < coordArrayEMA.size-1){
                coor[0] = coor[0] - (sin(Math.toRadians(dir)) * stepLength*10)
                coor[1] = coor[1] + (cos(Math.toRadians(dir)) * stepLength*10)
            }
        }


        if (coordArrayEMA.size > seqLengthEMA){
            coordArrayEMA.removeAt(0)
        }
        var xSum = 0.0
        var ySum = 0.0
        for (coor in coordArrayEMA){
            xSum += coor[0]
            ySum += coor[1]
        }
        xAvg = xSum/coordArrayEMA.size
        yAvg = ySum/coordArrayEMA.size

        return mapOf("statusCode" to statusCode, "outputCoor" to arrayListOf(xAvg, yAvg, rfLocalizationCoor[2]), "outputCoordArray" to coordArrayEMA)
    }
}
