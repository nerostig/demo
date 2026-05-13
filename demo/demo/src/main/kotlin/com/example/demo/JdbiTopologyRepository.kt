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

//        val topologyId = handle.createUpdate(
//            "INSERT INTO topologies DEFAULT VALUES"
//        )
//            .executeAndReturnGeneratedKeys("id")
//            .mapTo(Int::class.java)
//            .one()
        val topologyId = handle.createUpdate(
            "INSERT INTO topologies(name) VALUES (:name)"
        )
            .bind("name", topology.name)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .one()

        // Sensors
        topology.adjacency.keys.forEach { sensor ->
            handle.createUpdate(
                """
                INSERT INTO sensors(
                    id, topology_id, group_id, x, y, desired_duty_cycle, tolerance
                )
                VALUES (
                    :id, :topologyId, :groupId, :x, :y, :duty, :tol
                )
                """.trimIndent()
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

        // Edges (adjacency)
        topology.adjacency.forEach { (source, targets) ->
            targets.forEach { target ->
                handle.createUpdate(
                    """
                    INSERT INTO edges(topology_id, source_id, target_id)
                    VALUES (:tid, :src, :tgt)
                    """.trimIndent()
                )
                    .bind("tid", topologyId)
                    .bind("src", source.id)
                    .bind("tgt", target.id)
                    .execute()
            }
        }

        // Duty cycles
        topology.dutyCycles.forEach { (sensor, value) ->
            handle.createUpdate(
                """
                INSERT INTO duty_cycles(topology_id, sensor_id, value)
                VALUES (:tid, :sid, :value)
                """.trimIndent()
            )
                .bind("tid", topologyId)
                .bind("sid", sensor.id)
                .bind("value", value)
                .execute()
        }

        return topologyId
    }

    /* ================= FIND BY ID ================= */

    override fun findById(id: Int): ScheduledNetworkTopology? {

        // 1 Load sensors
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

        // 2 Load edges
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

        // 3 Load duty cycles
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

        return ScheduledNetworkTopology(
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
    override fun update(id: Int, topology: ScheduledNetworkTopology) {

        val exists = handle.createQuery(
            "SELECT 1 FROM topologies WHERE id = :id"
        )
            .bind("id", id)
            .mapTo(Int::class.java)
            .findOne()
            .isPresent

        if (!exists) throw TopologyNotFoundException(id)

        // limpar estado dependente
        handle.createUpdate("DELETE FROM edges WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        handle.createUpdate("DELETE FROM duty_cycles WHERE topology_id = :id")
            .bind("id", id)
            .execute()

        // atualizar sensores
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

        // reinserir edges
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

        // reinserir duty cycles
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
    }
    }