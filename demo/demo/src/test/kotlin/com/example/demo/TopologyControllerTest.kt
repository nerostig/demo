package com.example.demo


import com.example.demo.pipeline.*
import com.example.demo.services.TopologyService
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
class TopologyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var service: TopologyService

    private val objectMapper = ObjectMapper()

    private var savedTopologyId: Int? = null

    @BeforeEach
    fun setUp() {
        // Limpa o estado entre testes
        savedTopologyId = null
    }

    @AfterEach
    fun tearDown() {
        // Limpa dados criados durante os testes
        savedTopologyId?.let {
            try {
                mockMvc.perform(delete("/api/topology/$it"))
                    .andExpect(status().isNoContent)
            } catch (ex: Exception) {
                // Ignora erro se o dado já foi deletado
            }
        }
    }

    @Test
    fun `should save topology successfully`() {
        val request = TopologySaveRequest(
            id = null,
            name = "Test Topology",
            sensors = listOf(
                SensorSaveInput(
                    id = "S1",
                    x = 10,
                    y = 20,
                    desiredDutyCycle = 0.5,
                    tolerance = 0.1,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        val json = objectMapper.writeValueAsString(request)

        mockMvc.perform(post("/api/topology")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Test Topology"))

        // Salva o ID para limpeza
        val response = mockMvc.perform(post("/api/topology")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andReturn().response
        val content = response.contentAsString
        val id = objectMapper.readTree(content).get("id").asInt()
        savedTopologyId = id
    }

    @Test
    fun `should update topology successfully`() {
        // Primeiro salva uma topologia
        val initialRequest = TopologySaveRequest(
            id = null,
            name = "Original",
            sensors = listOf(
                SensorSaveInput(
                    id = "S1",
                    x = 10,
                    y = 20,
                    desiredDutyCycle = 0.5,
                    tolerance = 0.1,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        val json = objectMapper.writeValueAsString(initialRequest)
        val response = mockMvc.perform(post("/api/topology")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andReturn().response
        val content = response.contentAsString
        val id = objectMapper.readTree(content).get("id").asInt()
        savedTopologyId = id

        // Atualiza a topologia
        val updateRequest = TopologySaveRequest(
            id = id,
            name = "Updated",
            sensors = listOf(
                SensorSaveInput(
                    id = "S1",
                    x = 15,
                    y = 25,
                    desiredDutyCycle = 0.6,
                    tolerance = 0.2,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        mockMvc.perform(put("/api/topology/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
    }

    @Test
    fun `should delete topology successfully`() {
        // Cria uma topologia
        val request = TopologySaveRequest(
            id = null,
            name = "Test Topology",
            sensors = listOf(
                SensorSaveInput(
                    id = "S1",
                    x = 10,
                    y = 20,
                    desiredDutyCycle = 0.5,
                    tolerance = 0.1,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        val json = objectMapper.writeValueAsString(request)
        val response = mockMvc.perform(post("/api/topology/save")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andReturn().response
        val content = response.contentAsString
        val id = objectMapper.readTree(content).get("id").asInt()
        savedTopologyId = id

        // Deleta a topologia
        mockMvc.perform(delete("/api/topology/$id"))
            .andExpect(status().isNoContent)

        // Verifica que não existe mais
        mockMvc.perform(get("/api/topology/$id"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should implement topology successfully`() {
        val request = TopologyRequest(
            sensors = listOf(
                SensorInput(
                    id = "S1",
                    x = 10,
                    y = 20,
                    desiredDutyCycle = 0.5,
                    tolerance = 0.1,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        mockMvc.perform(post("/api/topology/planning")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sensors").isArray)
            .andExpect(jsonPath("$.sensors[0].id").value("S1"))
    }


    @Test
    fun `should return all topologies`() {
        // Cria duas topologias
        val request1 = TopologySaveRequest(
            id = null,
            name = "Topology 1",
            sensors = listOf(
                SensorSaveInput(
                    id = "S1",
                    x = 10,
                    y = 20,
                    desiredDutyCycle = 0.5,
                    tolerance = 0.1,
                    groupId = "G1"
                )
            ),
            adjacency = mapOf("S1" to listOf())
        )

        val request2 = TopologySaveRequest(
            id = null,
            name = "Topology 2",
            sensors = listOf(
                SensorSaveInput(
                    id = "S2",
                    x = 15,
                    y = 25,
                    desiredDutyCycle = 0.6,
                    tolerance = 0.2,
                    groupId = "G2"
                )
            ),
            adjacency = mapOf("S2" to listOf())
        )

        val json1 = objectMapper.writeValueAsString(request1)
        val json2 = objectMapper.writeValueAsString(request2)

        val response1 = mockMvc.perform(post("/api/topology")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json1))
            .andReturn().response
        val id1 = objectMapper.readTree(response1.contentAsString).get("id").asInt()

        val response2 = mockMvc.perform(post("/api/topology")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json2))
            .andReturn().response
        val id2 = objectMapper.readTree(response2.contentAsString).get("id").asInt()

        // Salva IDs para limpeza
        savedTopologyId = id1
        val tempId = id2

        // Verifica todas as topologias
        mockMvc.perform(get("/api/topology"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        // Limpa a segunda topologia
        mockMvc.perform(delete("/api/topology/$tempId"))
            .andExpect(status().isNoContent)
    }
}
