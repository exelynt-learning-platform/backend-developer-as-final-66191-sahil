package com.bookingsystem;

import com.bookingsystem.entity.Resource;
import com.bookingsystem.entity.Role;
import com.bookingsystem.entity.User;
import com.bookingsystem.repository.ResourceRepository;
import com.bookingsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ResourceRepository resourceRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long resourceId;

    @BeforeEach
    void setUp() {
        userRepository.findByUsername("t_admin").orElseGet(() -> userRepository.save(User.builder()
                .username("t_admin").password(passwordEncoder.encode("pass")).email("t_admin@test.com")
                .role(Role.ADMIN).enabled(true).build()));

        userRepository.findByUsername("t_user").orElseGet(() -> userRepository.save(User.builder()
                .username("t_user").password(passwordEncoder.encode("pass")).email("t_user@test.com")
                .role(Role.USER).enabled(true).build()));

        Resource resource = resourceRepository.save(Resource.builder()
                .name("Test Room").type("ROOM").capacity(4).available(true).build());
        resourceId = resource.getId();
    }

    private String loginAndGetToken(String username) throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("username", username, "password", "pass"))))
                .andExpect(status().isOk())
                .andReturn();
        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    void userCanCreateAndOnlySeeOwnReservations() throws Exception {
        String userToken = loginAndGetToken("t_user");

        String payload = objectMapper.writeValueAsString(Map.of(
                "resourceId", resourceId,
                "startTime", LocalDateTime.now().plusDays(1).toString(),
                "endTime", LocalDateTime.now().plusDays(1).plusHours(2).toString(),
                "price", 25.50
        ));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("t_user"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/reservations").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("t_user"));
    }

    @Test
    void userCannotDeleteReservation() throws Exception {
        String userToken = loginAndGetToken("t_user");
        mockMvc.perform(delete("/api/reservations/1").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/reservations")).andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanUpdateReservationStatus() throws Exception {
        String userToken = loginAndGetToken("t_user");
        String adminToken = loginAndGetToken("t_admin");
        String payload = objectMapper.writeValueAsString(Map.of(
                "resourceId", resourceId,
                "startTime", LocalDateTime.now().plusDays(2).toString(),
                "endTime", LocalDateTime.now().plusDays(2).plusHours(1).toString(),
                "price", 40.00
        ));

        String response = mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long reservationId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/reservations/" + reservationId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void resourceWithActiveReservationCannotBeDeleted() throws Exception {
        String userToken = loginAndGetToken("t_user");
        String adminToken = loginAndGetToken("t_admin");
        String payload = objectMapper.writeValueAsString(Map.of(
                "resourceId", resourceId,
                "startTime", LocalDateTime.now().plusDays(3).toString(),
                "endTime", LocalDateTime.now().plusDays(3).plusHours(1).toString(),
                "price", 50.00
        ));

        mockMvc.perform(post("/api/reservations")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/resources/" + resourceId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }
}
