package be.duft.easier_household.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HouseholdApiTest extends ApiIntegrationTest {

    private static final String HOME = """
            {"name": "Maison", "timeZone": "Europe/Brussels", "address": "Grand-Place 1, Bruxelles",
             "latitude": 50.8467, "longitude": 4.3525}
            """;

    @Test
    void create_returnsTheHouseholdAsTheFrontSeesIt() throws Exception {
        Account owner = register();

        mockMvc.perform(as(owner, post("/households"), HOME))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Maison"))
                .andExpect(jsonPath("$.timeZone").value("Europe/Brussels"))
                .andExpect(jsonPath("$.address").value("Grand-Place 1, Bruxelles"))
                .andExpect(jsonPath("$.latitude").value(50.8467))
                .andExpect(jsonPath("$.longitude").value(4.3525))
                .andExpect(jsonPath("$.createdByName").value("Ada Lovelace"))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.roomCount").value(0));
    }

    @Test
    void create_refusesInvalidBodies() throws Exception {
        Account owner = register();

        mockMvc.perform(as(owner, post("/households"), """
                        {"name": "Maison", "timeZone": "Mars/Olympus"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(owner, post("/households"), """
                        {"timeZone": "Europe/Brussels"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(owner, post("/households"), """
                        {"name": "Maison", "timeZone": "Europe/Brussels", "latitude": 50.8}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_andGet_showOnlyMyHouseholds() throws Exception {
        Account owner = register();
        Account stranger = register();
        Long id = createdId(mockMvc.perform(as(owner, post("/households"), HOME)).andReturn());
        mockMvc.perform(as(owner, post("/households/" + id + "/rooms"), """
                {"name": "Cuisine", "floor": 0, "type": "KITCHEN"}
                """)).andExpect(status().isCreated());

        mockMvc.perform(as(owner, get("/households")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())))
                .andExpect(jsonPath("$[0].memberCount").value(1))
                .andExpect(jsonPath("$[0].roomCount").value(1));
        mockMvc.perform(as(owner, get("/households/" + id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.roomCount").value(1));

        mockMvc.perform(as(stranger, get("/households")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(as(stranger, get("/households/" + id)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_isReservedToTheOwner() throws Exception {
        Account owner = register();
        Account stranger = register();
        Long id = createdId(mockMvc.perform(as(owner, post("/households"), HOME)).andReturn());

        mockMvc.perform(as(stranger, put("/households/" + id), """
                        {"name": "Volée"}
                        """))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(owner, put("/households/" + id), """
                        {"name": "Maison principale"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maison principale"))
                .andExpect(jsonPath("$.timeZone").value("Europe/Brussels"));
    }

    @Test
    void delete_isReservedToTheOwnerAndRemovesTheHousehold() throws Exception {
        Account owner = register();
        Account stranger = register();
        MvcResult created = mockMvc.perform(as(owner, post("/households"), HOME)).andReturn();
        Long id = createdId(created);

        mockMvc.perform(as(stranger, delete("/households/" + id)))
                .andExpect(status().isForbidden());
        mockMvc.perform(as(owner, delete("/households/" + id)))
                .andExpect(status().isNoContent());
        mockMvc.perform(as(owner, get("/households/" + id)))
                .andExpect(status().isNotFound());
    }
}
