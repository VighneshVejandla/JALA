package com.jala.backend.medicinephoto.controller;

import com.jala.backend.medicinephoto.service.MedicinePhotoService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicinePhotoController.class)
@Import(WebSecurityTestConfig.class)
class MedicinePhotoControllerTest extends WebSliceTestBase {

    @MockitoBean
    private MedicinePhotoService medicinePhotoService;

    @Test
    @DisplayName("anonymous request is 401")
    void anon() throws Exception {
        mockMvc.perform(get("/api/v1/medicine-photos")
                        .param("medicineEntryId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated user lists medicine photos")
    void list_ok() throws Exception {
        given(medicinePhotoService.getPhotos(any(), any(), any()))
                .willReturn(List.of());
        mockMvc.perform(get("/api/v1/medicine-photos")
                        .param("medicineEntryId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }
}
