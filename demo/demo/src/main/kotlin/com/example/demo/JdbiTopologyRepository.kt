package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.Sensor
import org.jdbi.v3.core.Handle
import org.springframework.stereotype.Component



class JdbiTopologyRepository(
    private val handle: Handle
) : TopologyRepository {

    /* ================= SAVE ================= */

    override fun save(topology: ScheduledNetworkTopology): Int {

        println("========== REPOSITORY SAVE START ==========")
        println("TOPOLOGY NAME: ${topology.name}")
        println("SENSORS COUNT: ${topology.adjacency.keys.size}")
        println("ADJACENCY SIZE: ${topology.adjacency.size}")
        println("DUTY CYCLES SIZE: ${topology.dutyCycles.size}")

        val topologyId = handle.createUpdate(
            "INSERT INTO topologies(name) VALUES (:name)"
        )
            .bind("name", topology.name)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .one()

        println("INSERTED TOPOLOGY ID = $topologyId")

        // ================= SENSORS =================
        topology.adjacency.keys.forEach { sensor ->
            println("INSERT SENSOR: ${sensor.id} (topology=$topologyId)")

            handle.createUpdate(
                """
            INSERT INTO sensors(
                id, topology_id, group_id, x, y, desired_duty_cycle, tolerance
            )
            VALUES (
                :id, :topologyId, :groupId, :x, :y, :duty, :tol
            )
            """
            )
                .bind("id", sensor.id)
                .bind("topologyId", topologyId)
                .bind("groupId", sensor.groupid)
                .bind("x", sensor.x)
                .bind("y", sensor.y)
                .bind("duty", sensor.desiredDutyCycle)
                .bind("tol", sensor.tolerance)
                .execute()
        }

        // ================= EDGES =================
        topology.adjacency.forEach { (source, targets) ->
            targets.forEach { target ->
                println("INSERT EDGE: ${source.id} -> ${target.id}")

                handle.createUpdate(
                    """
                INSERT INTO edges(topology_id, source_id, target_id)
                VALUES (:tid, :src, :tgt)
                """
                )
                    .bind("tid", topologyId)
                    .bind("src", source.id)
                    .bind("tgt", target.id)
                    .execute()
            }
        }

        // ================= DUTY CYCLES =================
        topology.dutyCycles.forEach { (sensor, value) ->
            println("INSERT DUTY CYCLE: ${sensor.id} = $value")

            handle.createUpdate(
                """
            INSERT INTO duty_cycles(topology_id, sensor_id, value)
            VALUES (:tid, :sid, :value)
            """
            )
                .bind("tid", topologyId)
                .bind("sid", sensor.id)
                .bind("value", value)
                .execute()
        }

        println("========== REPOSITORY SAVE END ==========")

        return topologyId
    }
    /* ================= FIND BY ID ================= */

    override fun findById(id: Int): ScheduledNetworkTopology? {

        // =========================
        // 0. LOAD TOPOLOGY NAME
        // =========================
        val name = handle.createQuery(
            """
        SELECT name
        FROM topologies
        WHERE id = :id
        """
        )
            .bind("id", id)
            .mapTo(String::class.java)
            .findOne()
            .orElse(null)

        if (name == null) return null

        // =========================
        // 1. LOAD SENSORS
        // =========================
        val sensors = handle.createQuery(
            """
        SELECT id, group_id, x, y, desired_duty_cycle, tolerance
        FROM sensors
        WHERE topology_id = :id
        """
        )
            .bind("id", id)
            .map { rs, _ ->
                Sensor(
                    id = rs.getString("id"),
                    groupid = rs.getString("group_id"),
                    x = rs.getInt("x").takeIf { !rs.wasNull() },
                    y = rs.getInt("y").takeIf { !rs.wasNull() },
                    desiredDutyCycle = rs.getDouble("desired_duty_cycle"),
                    tolerance = rs.getDouble("tolerance")
                )
            }
            .list()

        if (sensors.isEmpty()) return null

        val sensorById = sensors.associateBy { it.id }

        // =========================
        // 2. LOAD EDGES
        // =========================
        val adjacency: MutableMap<Sensor, MutableList<Sensor>> =
            sensors.associateWith { mutableListOf<Sensor>() }.toMutableMap()

        handle.createQuery(
            """
        SELECT source_id, target_id
        FROM edges
        WHERE topology_id = :id
        """
        )
            .bind("id", id)
            .map { rs, _ ->
                val src = sensorById[rs.getString("source_id")]!!
                val tgt = sensorById[rs.getString("target_id")]!!
                adjacency[src]!!.add(tgt)
            }
            .list()

        // =========================
        // 3. LOAD DUTY CYCLES
        // =========================
        val dutyCycles = handle.createQuery(
            """
        SELECT sensor_id, value
        FROM duty_cycles
        WHERE topology_id = :id
        """
        )
            .bind("id", id)
            .map { rs, _ ->
                val sensor = sensorById[rs.getString("sensor_id")]!!
                sensor to rs.getDouble("value").takeIf { !rs.wasNull() }
            }
            .toMap()

        // =========================
        // 4. RETURN DOMAIN
        // =========================
        return ScheduledNetworkTopology(
            name = name,
            adjacency = adjacency,
            dutyCycles = dutyCycles
        )
    }

    /* ================= UPDATE ================= */


    /* ================= DELETE ================= */

    override fun delete(id: Int) {

        handle.createUpdate("DELETE FROM duty_cycles WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        handle.createUpdate("DELETE FROM edges WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        handle.createUpdate("DELETE FROM sensors WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        handle.createUpdate("DELETE FROM topologies WHERE id = :id")
            .bind("id", id)
            .execute()
    }


    /* ================= FIND ALL ================= */

    override fun findAll(): Map<Int, ScheduledNetworkTopology> {

        val ids = handle.createQuery(
            "SELECT id FROM topologies"
        )
            .mapTo(Int::class.java)
            .list()

        return ids.associateWith { id ->
            findById(id)!!
        }
    }

    override fun update(id: Int, topology: ScheduledNetworkTopology): ScheduledNetworkTopology {

        val exists = handle.createQuery(
            "SELECT 1 FROM topologies WHERE id = :id"
        )
            .bind("id", id)
            .mapTo(Int::class.java)
            .findOne()
            .isPresent

        if (!exists) throw TopologyNotFoundException(id)

        handle.createUpdate(
            "UPDATE topologies SET name = :name WHERE id = :id"
        )
            .bind("id", id)
            .bind("name", topology.name)
            .execute()

        handle.createUpdate("DELETE FROM edges WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        handle.createUpdate("DELETE FROM duty_cycles WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        topology.adjacency.keys.forEach { sensor ->
            handle.createUpdate(
                """
            UPDATE sensors
            SET group_id = :groupId,
                x = :x,
                y = :y,
                desired_duty_cycle = :duty,
                tolerance = :tol
            WHERE topology_id = :tid AND id = :sid
            """
            )
                .bind("tid", id)
                .bind("sid", sensor.id)
                .bind("groupId", sensor.groupid)
                .bind("x", sensor.x)
                .bind("y", sensor.y)
                .bind("duty", sensor.desiredDutyCycle)
                .bind("tol", sensor.tolerance)
                .execute()
        }

        topology.adjacency.forEach { (src, targets) ->
            targets.forEach { tgt ->
                handle.createUpdate(
                    """
                INSERT INTO edges(topology_id, source_id, target_id)
                VALUES (:tid, :src, :tgt)
                """
                )
                    .bind("tid", id)
                    .bind("src", src.id)
                    .bind("tgt", tgt.id)
                    .execute()
            }
        }

        topology.dutyCycles.forEach { (sensor, value) ->
            handle.createUpdate(
                """
            INSERT INTO duty_cycles(topology_id, sensor_id, value)
            VALUES (:tid, :sid, :value)
            """
            )
                .bind("tid", id)
                .bind("sid", sensor.id)
                .bind("value", value)
                .execute()
        }

        return findById(id)
            ?: throw TopologyNotFoundException(id)
    }
}