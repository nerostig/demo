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
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var authHeader: String

    // ------------------------
    // AUTH SETUP
    // ------------------------

    private fun createUserAndLogin() {
        username = "test-user"
        password = "Test1234!"

        // 1. tenta criar user
        val createResponse = mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        UserCreateInputModel(username, password)
                    )
                )
        ).andReturn().response

        // aceita 201 (criado) OU 400 (já existe)
        if (createResponse.status != 201 && createResponse.status != 400) {
            error("Unexpected status on user creation: ${createResponse.status}")
        }

        val tokenResponse = mockMvc.perform(
            post("/api/users/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        UserCreateTokenInputModel(username, password)
                    )
                )
        )
            .andExpect(status().isOk)
            .andReturn()
            .response

        val json = objectMapper.readTree(tokenResponse.contentAsString)

        val token = json["token"]?.asText()
            ?: error("Token not found in response: $json")

        authHeader = "bearer $token"
    }

    @BeforeEach
    fun setUp() {
        savedTopologyId = null
        createUserAndLogin()

    }

    @AfterEach
    fun tearDown() {

        //  limpar topology
        savedTopologyId?.let {
            try {
                mockMvc.perform(delete("/api/topology/$it"))
                    .andExpect(status().isNoContent)
            } catch (_: Exception) {}
        }

        try {
            mockMvc.perform(
                post("/api/logout")
                    .header("Authorization", authHeader)
            )
                .andExpect(status().isNoContent)
        } catch (_: Exception) {}
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



        val response = mockMvc.perform(post("/api/topology")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Test Topology"))
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
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andReturn().response
        val content = response.contentAsString
        val id = objectMapper.readTree(content).get("id").asInt()

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
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))

        mockMvc.perform(get("/api/topology/$id")
            .header("Authorization", authHeader))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated"))
        savedTopologyId = id
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
        val response = mockMvc.perform(post("/api/topology")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andReturn().response
        val content = response.contentAsString
        val id = objectMapper.readTree(content).get("id").asInt()
        savedTopologyId = id

        // Apaga a topologia
        mockMvc.perform(delete("/api/topology/$id"))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/api/topology/$id").header("Authorization", authHeader))
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
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json1))
            .andReturn().response
        val id1 = objectMapper.readTree(response1.contentAsString).get("id").asInt()

        val response2 = mockMvc.perform(post("/api/topology")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json2))
            .andReturn().response
        val id2 = objectMapper.readTree(response2.contentAsString).get("id").asInt()

        savedTopologyId = id1
        val tempId = id2

        mockMvc.perform(get("/api/topology").header("Authorization", authHeader))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))

        mockMvc.perform(delete("/api/topology/$tempId").header("Authorization", authHeader))
            .andExpect(status().isNoContent)
    }
}
