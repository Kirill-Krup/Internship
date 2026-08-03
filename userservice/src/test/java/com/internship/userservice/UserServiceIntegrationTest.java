package com.internship.userservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.userservice.dto.CardInfoDTO;
import com.internship.userservice.dto.CreateCardInfoDTO;
import com.internship.userservice.dto.UserDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.profiles.active", () -> "local");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Timestamp pastBirthday() {
        return Timestamp.from(Instant.now().minus(8000, ChronoUnit.DAYS));
    }

    private Timestamp futureExpiration() {
        return Timestamp.from(Instant.now().plus(365, ChronoUnit.DAYS));
    }

    private UserDTO buildUser(String email) {
        CardInfoDTO card = new CardInfoDTO(
                null,
                "1234567890123456",
                "Alice Smith",
                futureExpiration(),
                true,
                null
        );
        return new UserDTO(
                null,
                "Alice",
                "Smith",
                pastBirthday(),
                email,
                true,
                List.of(card)
        );
    }

    @Test
    @DisplayName("Full flow")
    void testFullControllerToDbFlow() throws Exception {
        UserDTO payload = buildUser("alice@example.com");

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.cards").isArray())
                .andReturn();

        UserDTO created = objectMapper.readValue(createResult.getResponse().getContentAsString(),
                UserDTO.class);
        Long userId = created.getId();

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.cards.length()").value(1));

        mockMvc.perform(get("/api/v1/users")
                        .param("name", "Alice")
                        .param("surname", "Smith")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        UserDTO updatePayload = new UserDTO(
                userId,
                "AliceUpdated",
                "SmithUpdated",
                created.getBirthDate(),
                "alice.updated@example.com",
                true,
                created.getCards()
        );

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("AliceUpdated"))
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"));

        CreateCardInfoDTO secondCard = new CreateCardInfoDTO(
                "5555555555554444",
                userId,
                "Alice SmithUpdated",
                futureExpiration()
        );

        MvcResult cardResult = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondCard)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn();

        CardInfoDTO createdCard = objectMapper.readValue(
                cardResult.getResponse().getContentAsString(), CardInfoDTO.class);

        mockMvc.perform(get("/api/v1/cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/cards")
                        .param("name", "AliceUpdated")
                        .param("surname", "SmithUpdated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        mockMvc.perform(put("/api/v1/cards/{id}/deactivate", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(put("/api/v1/cards/{id}/activate", createdCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(put("/api/v1/users/{id}/deactivate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(put("/api/v1/users/{id}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(delete("/api/v1/cards/{id}", createdCard.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Card limit of 5 is enforced")
    void testCardLimit() throws Exception {
        UserDTO userWithoutCards = new UserDTO(
                null,
                "Bob",
                "Marley",
                pastBirthday(),
                "bob@example.com",
                true,
                List.of()
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithoutCards)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long userId = created.get("id").asLong();

        for (int i = 0; i < 5; i++) {
            CreateCardInfoDTO card = new CreateCardInfoDTO(
                    "400000000000000" + i,
                    userId,
                    "Bob Marley",
                    futureExpiration()
            );
            mockMvc.perform(post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(card)))
                    .andExpect(status().isCreated());
        }

        CreateCardInfoDTO sixth = new CreateCardInfoDTO(
                "4000000000000099",
                userId,
                "Bob Marley",
                futureExpiration()
        );

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sixth)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode cards = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(cards).hasSize(5);
                });

        mockMvc.perform(get("/api/v1/cards")
                        .param("name", "Bob")
                        .param("surname", "Marley")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}