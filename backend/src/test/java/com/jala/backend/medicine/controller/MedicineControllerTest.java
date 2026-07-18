package com.jala.backend.medicine.controller;

import com.jala.backend.medicine.dto.request.CreateMedicineRequest;
import com.jala.backend.medicine.dto.response.MedicineResponse;
import com.jala.backend.medicine.enums.MedicineUnit;
import com.jala.backend.medicine.service.MedicineService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicineController.class)
@Import(WebSecurityTestConfig.class)
class MedicineControllerTest extends WebSliceTestBase {

    @MockitoBean
    private MedicineService medicineService;

    private CreateMedicineRequest validCreate() {
        CreateMedicineRequest r = new CreateMedicineRequest();
        r.setPondCycleId(UUID.randomUUID());
        r.setQuantity(new BigDecimal("5.0"));
        r.setUnit(MedicineUnit.ML);
        return r;
    }

    @Test
    @DisplayName("anonymous list is 401")
    void list_anon() throws Exception {
        mockMvc.perform(get("/api/v1/medicines")
                        .param("pondCycleId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker lists medicines")
    void list_worker_ok() throws Exception {
        given(medicineService.getMedicines(any(), any(), any()))
                .willReturn(List.of());
        mockMvc.perform(get("/api/v1/medicines")
                        .param("pondCycleId", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("worker creates a medicine entry (201)")
    void create_worker_created() throws Exception {
        given(medicineService.createMedicine(any()))
                .willReturn(MedicineResponse.builder().build());
        mockMvc.perform(post("/api/v1/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate())))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    @DisplayName("driver cannot create a medicine entry (403)")
    void create_driver_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
    }
}
