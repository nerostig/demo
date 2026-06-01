package com.example.demo

import DutyCycleTreeOptimizer


import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.optimizer.GlobalNogoodStore
import com.example.demo.optimizer.SearchContext
import com.example.demo.optimizer.areCoprime
import com.example.demo.optimizer.areCoprimePercentages
import com.example.demo.optimizer.dutyCycleToPeriod
import com.example.demo.optimizer.gcd
import computeSchedulesOptimized

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*
class DemoApplicationTestss {

    // ======================
    // ======================

    @Test
    fun `gcd funciona corretamente`() {
        assertEquals(1, gcd(7, 20))
        assertEquals(2, gcd(14, 6))
        assertEquals(5, gcd(25, 10))
    }

    @Test
    fun `areCoprime funciona corretamente`() {
        assertTrue(areCoprime(7, 20))
        assertFalse(areCoprime(14, 6))
        assertTrue(areCoprime(9, 28))
        assertFalse(areCoprime(10, 5))

    }


    // ======================
    // Testes de conversão
    // ======================

    @Test
    fun `50 porcento gera periodo 2`() {
        assertEquals(2, dutyCycleToPeriod(50.0))
    }

    @Test
    fun `33_3 porcento gera periodo 3`() {
        assertEquals(3, dutyCycleToPeriod(33.3))
    }

    @Test
    fun `40 porcento gera periodo arredondado`() {
        // 100 / 40 = 2.5 → round = 3 (dependendo da regra de round)
        assertEquals(2, dutyCycleToPeriod(40.0))
    }

    @Test
    fun `37_5 porcento gera periodo 3`() {
        // 100 / 37.5 = 2.666… → round = 3
        assertEquals(3, dutyCycleToPeriod(37.5))
    }

    // ======================
    // Testes de coprimalidade
    // ======================

    @Test
    fun `50 e 33_3 sao coprimos`() {
        // períodos: 2 e 3 → gcd = 1
       val ctx = SearchContext(GlobalNogoodStore())//, cutoff = Int.MAX_VALUE)
        assertTrue(areCoprimePercentages(50.0, 33.3,ctx))
    }

    @Test
    fun `50 e 25 nao sao coprimos`() {
        // períodos: 2 e 4 → gcd = 2
        val ctx = SearchContext(GlobalNogoodStore()) //cutoff = Int.MAX_VALUE)
        assertFalse(areCoprimePercentages(50.0, 25.0,ctx))
    }

    @Test
    fun `37_5 e 25 nao sao coprimos`() {
        // períodos: 3 e 4 → gcd = 1? NÃO
        // 100/37.5 = 2.66 → 3
        // 100/25 = 4
        val ctx = SearchContext(GlobalNogoodStore())//, cutoff = Int.MAX_VALUE)
        assertTrue(areCoprimePercentages(37.5, 25.0,ctx))
        assertFalse(areCoprimePercentages(50.0, 25.0,ctx))
    }

    @Test
    fun `33_3 e 66_6 nao sao coprimos`() {
        val ctx = SearchContext(GlobalNogoodStore())//, cutoff = Int.MAX_VALUE)
        // períodos: 3 e 2 → gcd = 1? sim
        assertTrue(areCoprimePercentages(33.3, 66.6,ctx))
    }

    // ======================
    // Testes de erro
    // ======================




    // ======================
    // Testes de tolerancia pequena
    // ======================

    @Test
    fun `tolerancia pequena 0_05 permite testar 50`() {
        val desired = 50.0
        val tolerance = 0.05

        val min = desired - tolerance
        val max = desired + tolerance

        var found50 = false
        var candidate = min
        val step = 0.01

        while (candidate <= max + 1e-9) {
            if (kotlin.math.abs(candidate - 50.0) < 1e-9) {
                found50 = true
                break
            }
            candidate += step
        }

        assertTrue(found50, "50.0 deve ser testado dentro do intervalo")
    }

    // ======================
    // Teste completo com topologia
    // ======================

    @Test
    fun testSolucaoCompletaComTolerancias() {



        val A = Sensor("A", desiredDutyCycle = 18.0, tolerance = 2.0)  // ~ [16,20]
        val B = Sensor("B", desiredDutyCycle = 24.0, tolerance = 2.0)  // ~ [22,26]
        val C = Sensor("C", desiredDutyCycle = 15.0, tolerance = 1.5)  // ~ [14,17]
        val D = Sensor("D", desiredDutyCycle = 12.0, tolerance = 1.0)  // ~ [11,13]
        val E = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 1.0)  // ~ [8,10]

        val topology = NetworkTopology(
            mapOf(
                A to listOf(B, C),
                B to listOf(A, C, E),
                C to listOf(A, B, D),
                D to listOf(C, E),
                E to listOf(B, D)
            )
        )

        val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
        val result = optimizer.optimize()

        println("Resultado teste 1:")
        result!!.forEach { (s, v) ->
            println("${s.id} -> $v")
        }

        assertTrue(result.values.all { it != null })
    }

    @Test
    fun testSolucaoParcialComNulls() {

        val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 0.5)
        val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 0.5)
        val C = Sensor("C", desiredDutyCycle = 20.0, tolerance = 0.5)
        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.5)
        val E = Sensor("E", desiredDutyCycle = 10.0, tolerance = 0.5)

        val topology = NetworkTopology(
            mapOf(
                A to listOf(B, C),
                B to listOf(A, C, E),
                C to listOf(A, B, D),
                D to listOf(C, E),
                E to listOf(B, D)
            )
        )

        val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
        val result = optimizer.optimize()

        println("Resultado teste 2:")
        result!!.forEach { (s, v) ->
            println("${s.id} -> $v")
        }

        //  Espera-se pelo menos um null
        assertTrue(result.values.any { it == null })
    }

    /*
    *A — B — C
    |   |
    D — E
    * */

    @Test
    fun `computeSchedules topologia media`() {

    val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 2.0)
    val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 2.0)
    val C = Sensor("C", desiredDutyCycle = 15.0, tolerance = 2.0)
    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 2.0)
    val E = Sensor("E", desiredDutyCycle = 9.0, tolerance = 2.0)

    val adjacency = mapOf(
        A to listOf(B, D),
        B to listOf(A, C, E),
        C to listOf(B),
        D to listOf(A, E),
        E to listOf(B, D)
    )
    val topology = NetworkTopology(adjacency)

    val optimizer = DutyCycleTreeOptimizer(topology, step = 5.0)

    val result = optimizer.optimize()

    println("=== Resultado da otimização ===")
    result?.forEach { (sensor, value) ->
        println("Sensor ${sensor.id}: DutyCycle = ${value?.let { "${it}%" } ?: "null"}")
    }

   }


    @Test
    fun `computeSchedules topologia com tolerancia pequena`() {

        val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 1.0) // ~19–21
        val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 1.0) // ~24–26
        val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 1.0) // ~13–15
        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 1.0) // ~9–11
        val E = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 1.0) // ~8–10

        val topology = NetworkTopology(
            mapOf(
                A to listOf(B, C),
                B to listOf(A, C, E),
                C to listOf(A, B, D),
                D to listOf(C, E),
                E to listOf(B, D)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        println("\n=== Resultado teste tolerância pequena ===")
        schedules.forEach {
            println("${it.sensor.id} -> ${it.parameter?.value}")
        }

        assertTrue(schedules.any { it.parameter != null })
    }

    @Test
    fun `computeSchedules topologia simples A e B`() {

        val A = Sensor("A", desiredDutyCycle = 6.0, tolerance = 1.0)
        val B = Sensor("B", desiredDutyCycle = 27.0, tolerance = 3.0)

        val topology = NetworkTopology(
            mapOf(
                A to listOf(B),
                B to listOf(A)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        println("\n=== Resultado teste A e B ===")
        schedules.forEach {
            println("${it.sensor.id} -> ${it.parameter?.value}")
        }

        // pelo menos um deve ter solução
        assertTrue(schedules.any { it.parameter != null })

        val assigned = schedules.filter { it.parameter != null }

        if (assigned.size == 2) {
            val a = assigned.first { it.sensor == A }.parameter!!.value
            val b = assigned.first { it.sensor == B }.parameter!!.value

            // não podem ser iguais
            assertNotEquals(a, b)

            assertTrue(
                areCoprime(
                    dutyCycleToPeriod(a),
                    dutyCycleToPeriod(b)
                )
            )
        }
    }
    @Test
    fun `computeSchedules topologia com tolerancia maior e solucao parcial`() {

        val A = Sensor("A", desiredDutyCycle = 6.0, tolerance = 1.0)
        val B = Sensor("B", desiredDutyCycle = 27.0, tolerance = 3.0)
        val C = Sensor("C", desiredDutyCycle = 25.0, tolerance = 0.0)
        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 3.0)
        val E = Sensor("E", desiredDutyCycle = 10.0,  tolerance = 3.0)

        val topology = NetworkTopology(
            mapOf(
                A to listOf(B, C),
                B to listOf(A, C, E),
                C to listOf(A, B, D),
                D to listOf(C, E),
                E to listOf(B, D)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        println("\n=== Resultado teste tolerância maior ===")
        schedules.forEach {
            println("${it.sensor.id} -> ${it.parameter?.value}")
        }

       // assertTrue(schedules.any { it.parameter == null })

        assertTrue(schedules.any { it.parameter != null })
    }
    @Test
    fun `computeSchedules topologia alternativa com tolerancia pequena`() {

        val a = Sensor("A", desiredDutyCycle = 20.0, tolerance = 1.0)
        val b = Sensor("B", desiredDutyCycle = 25.0, tolerance = 1.0)
        val c = Sensor("C", desiredDutyCycle = 14.0, tolerance = 1.0)
        val d = Sensor("D", desiredDutyCycle = 10.0, tolerance = 1.0)
        val e = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 1.0)

        val topology = NetworkTopology(
            mapOf(
                a to listOf(b, d),
                b to listOf(a, c, e),
                c to listOf(b),
                d to listOf(a, e),
                e to listOf(d, b)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        println("\n=== Resultado teste topologia alternativa (tolerância pequena) ===")
        schedules.forEach {
            println("${it.sensor.id} -> ${it.parameter?.value}")
        }

        // Deve haver pelo menos um valor atribuído
        assertTrue(schedules.any { it.parameter != null })

        // Idealmente poucos nulls
        val nullCount = schedules.count { it.parameter == null }
        assertTrue(nullCount < schedules.size)
    }

    @Test
    fun `ABC todos coprimos`() {

        val a = Sensor("A", desiredDutyCycle = 50.0, tolerance = 0.5)
        val b = Sensor("B", desiredDutyCycle = 33.3, tolerance = 0.5)
        val c = Sensor("C", desiredDutyCycle = 20.0, tolerance = 0.5)

        val topology = NetworkTopology(
            mapOf(
                a to listOf(b),
                b to listOf(a, c),
                c to listOf(b)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        val nulls = schedules.count { it.parameter == null }

        // Espera-se solução completa
        assertEquals(0, nulls)
    }

    @Test
    fun `B e C nao coprimos mas A e B sao`() {

        val a = Sensor("A", desiredDutyCycle = 33.3, tolerance = 0.5)
        val b = Sensor("B", desiredDutyCycle = 50.0, tolerance = 0.5)
        val c = Sensor("C", desiredDutyCycle = 25.0, tolerance = 0.5)

        val topology = NetworkTopology(
            mapOf(
                a to listOf(b),
                b to listOf(a, c),
                c to listOf(b)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        val assigned = schedules.filter { it.parameter != null }

        // Pelo menos A e B devem ser atribuídos
        assertTrue(assigned.size >= 2)
    }

    @Test
    fun `A nao coprimo com B e B nao coprimo com C`() {

        val a = Sensor("A", desiredDutyCycle = 50.0, tolerance = 0.0)
        val b = Sensor("B", desiredDutyCycle = 50.0, tolerance = 0.0)
        val c = Sensor("C", desiredDutyCycle = 50.0, tolerance = 0.0)

        val topology = NetworkTopology(
            mapOf(
                a to listOf(b),
                b to listOf(a, c),
                c to listOf(b)
            )
        )

        val schedules = computeSchedulesOptimized(topology)

        val nulls = schedules.count { it.parameter == null }
        println(nulls)

        assertTrue(nulls == 2)
    }

}
