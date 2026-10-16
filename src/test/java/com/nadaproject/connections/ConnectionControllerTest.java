package com.nadaproject.connections;

import com.nadaproject.connections.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreatePersonAndContacts() throws Exception {
        
        MvcResult res1 = mockMvc.perform(post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Asha\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Asha"))
                .andReturn();

        Person asha = objectMapper.readValue(res1.getResponse().getContentAsString(), Person.class);

        MvcResult res2 = mockMvc.perform(post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Rohan\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Person rohan = objectMapper.readValue(res2.getResponse().getContentAsString(), Person.class);

        mockMvc.perform(post("/knows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("a", asha.getId(), "b", rohan.getId()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/people/" + asha.getId() + "/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Rohan"));
    }

    @Test
    void testPathWithinThreeHopsAndExceedingLimit() throws Exception {
        long idA = createPersonHelper("Asha");
        long idB = createPersonHelper("Meera");
        long idC = createPersonHelper("Kabir");
        long idD = createPersonHelper("Rohan");
        long idE = createPersonHelper("Dev");

        connectHelper(idA, idB);
        connectHelper(idB, idC);
        connectHelper(idC, idD);
        connectHelper(idD, idE);

        mockMvc.perform(get("/path").param("from", String.valueOf(idA)).param("to", String.valueOf(idD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.hops").value(3))
                .andExpect(jsonPath("$.path.length()").value(4));

        mockMvc.perform(get("/path").param("from", String.valueOf(idA)).param("to", String.valueOf(idE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void testHindiScriptSupport() throws Exception {
        mockMvc.perform(post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"आशा\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("आशा"));
    }

    @Test
    void testNotFoundAndValidationErrors() throws Exception {
        mockMvc.perform(get("/people/9/contacts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("no person 9"));

        mockMvc.perform(post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    private long createPersonHelper(String name) throws Exception {
        MvcResult res = mockMvc.perform(post("/people")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"" + name + "\"}"))
                .andReturn();
        Person p = objectMapper.readValue(res.getResponse().getContentAsString(), Person.class);
        return p.getId();
    }

    private void connectHelper(long a, long b) throws Exception {
        mockMvc.perform(post("/knows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("a", a, "b", b))))
                .andExpect(status().isNoContent());
    }
}
