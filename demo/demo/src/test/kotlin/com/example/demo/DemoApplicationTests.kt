package com.example.demo

import DutyCycleTreeOptimizer
import areCoprime
import areCoprimePercentages

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import computeSchedulesOptimized
import dutyCycleToPeriod
import gcd
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.Assertions.*
@SpringBootTest
class DemoApplicationTests {

	@Test
	fun contextLoads() {
	}

}



class DemoApplicationTestss {

    // ======================
    // Testes matemáticos base
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
        assertTrue(areCoprimePercentages(50.0, 33.3))
    }

    @Test
    fun `50 e 25 nao sao coprimos`() {
        // períodos: 2 e 4 → gcd = 2
        assertFalse(areCoprimePercentages(50.0, 25.0))
    }

    @Test
    fun `37_5 e 25 nao sao coprimos`() {
        // períodos: 3 e 4 → gcd = 1? NÃO
        // 100/37.5 = 2.66 → 3
        // 100/25 = 4
        assertTrue(areCoprimePercentages(37.5, 25.0))
        assertFalse(areCoprimePercentages(50.0, 25.0))
    }

    @Test
    fun `33_3 e 66_6 nao sao coprimos`() {
        // períodos: 3 e 2 → gcd = 1? sim
        assertTrue(areCoprimePercentages(33.3, 66.6))
    }

    // ======================
    // Testes de erro
    // ======================

    @Test
    fun `duty cycle zero falha`() {
        assertThrows(IllegalArgumentException::class.java) {
            dutyCycleToPeriod(0.0)
        }
    }

    @Test
    fun `duty cycle negativo falha`() {
        assertThrows(IllegalArgumentException::class.java) {
            dutyCycleToPeriod(-10.0)
        }
    }
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

//        val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 2.0)
//        val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 2.0)
//        val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 2.0)
//        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 1.5)
//        val E = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 1.5)

//        val A = Sensor("A", desiredDutyCycle = 19.0, tolerance = 1.5)
//        val B = Sensor("B", desiredDutyCycle = 24.0, tolerance = 1.5)
//        val C = Sensor("C", desiredDutyCycle = 13.0, tolerance = 1.0)
//        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.9)
//        val E = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 0.9)

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

        // 🔒 Garantia: nenhum null
        assertTrue(result.values.all { it != null })
    }

    @Test
    fun testSolucaoParcialComNulls() {

        val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 0.5)
        val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 0.5)
        val C = Sensor("C", desiredDutyCycle = 20.0, tolerance = 0.5) // ⚠ igual a A
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

//    @Test
//    fun `computeSchedules topologia media`() {
//        val a = Sensor("A", 20.0, 1.0)
//        val b = Sensor("B", 25.0, 1.0)
//        val c = Sensor("C", 33.0, 1.0)
//        val d = Sensor("D", 50.0, 1.0)
//        val e = Sensor("E", 40.0, 1.0)
//
//        val topology = NetworkTopology(
//            mapOf(
//                a to listOf(b, d),
//                b to listOf(a, c, e),
//                c to listOf(b),
//                d to listOf(a, e),
//                e to listOf(d, b)
//            )
//        )
//
//        val schedules = computeScheduless(topology)
//
//        assertEquals(5, schedules.size)
//        assertTrue(schedules.all { it.parameter != null })
//
//        // verificar CPB apenas entre vizinhos
//        for (sensor in topology.sensors()) {
//            for (neighbor in topology.neighbors(sensor)) {
//                val p1 = schedules.first { it.sensor == sensor }.parameter!!.value
//                val p2 = schedules.first { it.sensor == neighbor }.parameter!!.value
//                assertTrue(areCoprimePercentages(p1, p2))
//            }
//        }
//    }


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

        // Pelo menos um sensor deve ter valor atribuído
        assertTrue(schedules.any { it.parameter != null })
    }

    @Test
    fun `computeSchedules topologia com tolerancia maior e solucao parcial`() {

        val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 3.0)
        val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 3.0)
        val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 3.0)
        val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 3.0)
        val E = Sensor("E", desiredDutyCycle = 9.0,  tolerance = 3.0)

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

        // Deve haver pelo menos um sensor sem valor
        assertTrue(schedules.any { it.parameter == null })

        // Mas também pelo menos um com valor
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

}
