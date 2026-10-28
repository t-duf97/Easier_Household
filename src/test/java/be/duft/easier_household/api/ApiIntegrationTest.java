package be.duft.easier_household.api;

import be.duft.easier_household.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base des tests HTTP de bout en bout : vraie chaîne de sécurité (JwtFilter), vrais controllers et services,
 * PostgreSQL Testcontainers. Le JSON est vérifié tel que le front le reçoit.
 */
@SpringBootTest(properties = "jwt.secret=dGVzdC1zZWNyZXQtZm9yLWludGVncmF0aW9uLXRlc3RzLW9ubHktbm90LXJlYWwtMTIzNDU2")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class ApiIntegrationTest {

    protected static final String PASSWORD = "motdepasse-de-test";

    @Autowired
    protected MockMvc mockMvc;

    /** Un compte inscrit via l'API, avec son token. */
    protected record Account(String email, Long userId, String token) {
        String bearer() {
            return "Bearer " + token;
        }
    }

    protected Account register() throws Exception {
        String email = "api-" + UUID.randomUUID() + "@test.local";
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "firstName": "Ada", "lastName": "Lovelace"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return new Account(email, ((Number) JsonPath.read(body, "$.userId")).longValue(), JsonPath.read(body, "$.accessToken"));
    }

    /** Envoie {@code json} en tant que {@code account}. */
    protected MockHttpServletRequestBuilder as(Account account, MockHttpServletRequestBuilder request, String json) {
        return request.header("Authorization", account.bearer()).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    protected MockHttpServletRequestBuilder as(Account account, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", account.bearer());
    }

    protected Long createdId(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
}
