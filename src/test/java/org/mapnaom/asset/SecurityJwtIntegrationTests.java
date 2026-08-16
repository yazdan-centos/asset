package org.mapnaom.asset;

import org.junit.jupiter.api.Test;
import org.mapnaom.asset.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityJwtIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void jwtAuthenticationLoadsDatabaseRolesAndEnforcesAuthorization() throws Exception {
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());

        String adminToken = login("admin", "admin12345");
        assertThat(adminToken.split("\\.")).hasSize(3);

        String createdUser = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reader",
                                  "password": "reader123",
                                  "enabled": true,
                                  "roles": ["USER"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long readerId = objectMapper.readTree(createdUser).get("id").asLong();

        assertThat(userRepository.findByUsername("reader"))
                .get()
                .satisfies(user -> {
                    assertThat(user.getPasswordHash()).isNotEqualTo("reader123");
                    assertThat(user.getRoles()).extracting("name").containsExactly("ROLE_USER");
                });

        String readerToken = login("reader", "reader123");
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer " + readerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cost-centers")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CC-SEC","name":"Secured","active":true}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/users/{id}", readerId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "reader",
                                  "enabled": true,
                                  "roles": ["USER", "ADMIN"]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cost-centers")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CC-SEC","name":"Secured","active":true}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/assets")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode body = objectMapper.readTree(response);
        assertThat(body.has("refreshToken")).isFalse();
        return body.get("accessToken").asText();
    }
}
