package be.duft.easier_household.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthApiTest extends ApiIntegrationTest {

    @Test
    void register_returnsTokenAndUserFields() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "grace-%d@test.local", "password": "%s", "firstName": "Grace", "lastName": "Hopper"}
                                """.formatted(System.nanoTime(), PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.lastName").value("Hopper"))
                .andExpect(jsonPath("$.userName").doesNotExist());
    }

    @Test
    void register_refusesAlreadyUsedEmailWithConflict() throws Exception {
        Account existing = register();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "firstName": "Ada", "lastName": "Bis"}
                                """.formatted(existing.email(), PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void register_refusesInvalidBody() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "pas-un-email", "password": "court", "firstName": "", "lastName": "X"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", allOf(containsString("email"), containsString("password"))));
    }

    @Test
    void login_returnsTokenForValidCredentials() throws Exception {
        Account account = register();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(account.email(), PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.userId").value(account.userId()));
    }

    @Test
    void login_givesTheSameUnauthorizedAnswerForWrongPasswordAndUnknownEmail() throws Exception {
        Account account = register();

        String wrongPassword = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "mauvais-mot-de-passe"}
                                """.formatted(account.email())))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownEmail = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "inconnu-%d@test.local", "password": "%s"}
                                """.formatted(System.nanoTime(), PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(com.jayway.jsonpath.JsonPath.<String>read(unknownEmail, "$.message"))
                .as("ne pas révéler si l'email existe")
                .isEqualTo(com.jayway.jsonpath.JsonPath.<String>read(wrongPassword, "$.message"));
    }

    @Test
    void protectedRoute_requiresAValidToken() throws Exception {
        Account account = register();

        mockMvc.perform(get("/households"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/households").header("Authorization", "Bearer pas-un-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/households").header("Authorization", "abc"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/households").header("Authorization", account.bearer()))
                .andExpect(status().isOk());
    }

    @Test
    void swagger_isPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
