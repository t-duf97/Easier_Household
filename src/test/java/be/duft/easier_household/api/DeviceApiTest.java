package be.duft.easier_household.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceApiTest extends ApiIntegrationTest {

    private Account owner;
    private Account stranger;
    private Long householdId;
    private Long roomId;

    @BeforeEach
    void setUp() throws Exception {
        owner = register();
        stranger = register();
        householdId = createdId(mockMvc.perform(as(owner, post("/households"), """
                {"name": "Maison", "timeZone": "Europe/Brussels"}
                """)).andReturn());
        roomId = createdId(mockMvc.perform(as(owner, post("/households/" + householdId + "/rooms"), """
                        {"name": "Salon", "floor": 0, "type": "LIVING_ROOM"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Salon"))
                .andExpect(jsonPath("$.type").value("LIVING_ROOM"))
                .andExpect(jsonPath("$.floor").value(0))
                .andExpect(jsonPath("$.householdId").value(householdId))
                .andExpect(jsonPath("$.deviceCount").value(0))
                .andReturn());
    }

    @Test
    void create_returnsTheDeviceAsTheFrontSeesIt() throws Exception {
        mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                        {"name": "Lampe", "type": "LIGHT"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Lampe"))
                .andExpect(jsonPath("$.type").value("LIGHT"))
                .andExpect(jsonPath("$.status").value("OFF"))
                .andExpect(jsonPath("$.online").value(true))
                .andExpect(jsonPath("$.roomId").value(roomId))
                .andExpect(jsonPath("$.roomName").value("Salon"))
                .andExpect(jsonPath("$.householdName").value("Maison"));

        mockMvc.perform(as(owner, get("/rooms/" + roomId)))
                .andExpect(jsonPath("$.deviceCount").value(1));
        mockMvc.perform(as(owner, get("/rooms/" + roomId + "/devices")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void create_refusesInvalidBodies() throws Exception {
        mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                        {"type": "LIGHT"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                        {"name": "Grille-pain", "type": "TOASTER"}
                        """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), "{ pas du json"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(as(owner, post("/households/" + householdId + "/rooms"), """
                        {"name": "Sans type"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeState_updatesTheDeviceAndRecordsAManualChangeByMe() throws Exception {
        Long deviceId = createdId(mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                {"name": "Thermostat", "type": "THERMOSTAT"}
                """)).andReturn());

        mockMvc.perform(as(owner, patch("/devices/" + deviceId + "/state"), """
                        {"status": "ON", "value": "21.5", "source": "EXTERNAL_API"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON"))
                .andExpect(jsonPath("$.currentValue").value("21.5"));

        mockMvc.perform(as(owner, get("/devices/" + deviceId + "/history?page=0&pageSize=5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ON"))
                .andExpect(jsonPath("$.content[0].value").value("21.5"))
                .andExpect(jsonPath("$.content[0].changeSource").value("MANUAL"))
                .andExpect(jsonPath("$.content[0].changedBy").value(owner.userId()));

        mockMvc.perform(as(owner, patch("/devices/" + deviceId + "/state"), """
                        {"value": "22"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_renamesAndDelete_removesTheDevice() throws Exception {
        Long deviceId = createdId(mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                {"name": "Lampe", "type": "LIGHT"}
                """)).andReturn());

        mockMvc.perform(as(owner, patch("/devices/" + deviceId), """
                        {"name": "Lampe de chevet"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lampe de chevet"))
                .andExpect(jsonPath("$.type").value("LIGHT"));

        mockMvc.perform(as(owner, delete("/devices/" + deviceId))).andExpect(status().isNoContent());
        mockMvc.perform(as(owner, get("/devices/" + deviceId))).andExpect(status().isNotFound());
        mockMvc.perform(as(owner, delete("/rooms/" + roomId))).andExpect(status().isNoContent());
        mockMvc.perform(as(owner, get("/rooms/" + roomId))).andExpect(status().isNotFound());
    }

    @Test
    void stranger_cannotReadNorControlMyDevices() throws Exception {
        Long deviceId = createdId(mockMvc.perform(as(owner, post("/rooms/" + roomId + "/devices"), """
                {"name": "Lampe", "type": "LIGHT"}
                """)).andReturn());

        mockMvc.perform(as(stranger, get("/devices/" + deviceId))).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, get("/rooms/" + roomId + "/devices"))).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, get("/devices/" + deviceId + "/history"))).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, patch("/devices/" + deviceId + "/state"), """
                {"status": "ON"}
                """)).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, patch("/devices/" + deviceId), """
                {"name": "Piratée"}
                """)).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, post("/rooms/" + roomId + "/devices"), """
                {"name": "Intrus", "type": "LIGHT"}
                """)).andExpect(status().isForbidden());
        mockMvc.perform(as(stranger, delete("/devices/" + deviceId))).andExpect(status().isForbidden());

        mockMvc.perform(as(owner, get("/devices/" + deviceId)))
                .andExpect(jsonPath("$.status").value("OFF"))
                .andExpect(jsonPath("$.name").value("Lampe"));
    }
}
