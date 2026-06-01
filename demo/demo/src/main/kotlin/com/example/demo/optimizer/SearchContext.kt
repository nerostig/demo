package com.example.demo.optimizer

import com.example.demo.domain.Sensor

class SearchContext(val globalNogoods: GlobalNogoodStore){


    val coprimeCache = mutableMapOf<Pair<Double, Double>, Boolean>()
    val impactCache = mutableMapOf<Triple<Sensor, Int, Sensor>, Int>()
    val localNogoods  = mutableListOf<Nogood>()

}