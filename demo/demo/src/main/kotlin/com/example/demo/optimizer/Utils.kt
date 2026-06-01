package com.example.demo.optimizer

import com.example.demo.domain.Sensor
import kotlin.math.abs
import kotlin.math.round


// ===================== MATEMÁTICA =====================
fun gcd(a: Int, b: Int): Int {
    //println("$a")
    //println("$b")
    var x = abs(a)
    var y = abs(b)
    while (y != 0) {
        val t = y
        y = x % y
        x = t
    }
    return x
}

fun areCoprime(a: Int, b: Int): Boolean = gcd(a, b) == 1
fun dutyCycleToPeriod(dc: Double): Int = round(100.0 / dc).toInt()
fun areCoprimePercentagess(a: Double, b: Double): Boolean {

    return areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))


}

fun areCoprimePercentages(
    p1: Double,
    p2: Double,
    ctx: SearchContext
): Boolean {
    val key = Pair(p1, p2)
    return ctx.coprimeCache.getOrPut(key) {
        areCoprime(dutyCycleToPeriod(p1), dutyCycleToPeriod(p2))
    }
}

// =====================  =====================



fun generateCandidates(sensor: Sensor): List<Int> {
    val minPeriod = kotlin.math.round(
        100.0 / (sensor.desiredDutyCycle + sensor.tolerance)
    ).toInt()

    println("${sensor.id }")
    println(sensor.desiredDutyCycle - sensor.tolerance)
    val maxPeriod = kotlin.math.round(
        100.0 / (sensor.desiredDutyCycle - sensor.tolerance)
    ).toInt()
    println("${sensor.id }> $minPeriod e $maxPeriod")

    return (minPeriod..maxPeriod).toList()
}

