package com.jala.backend.storage.controller;

import com.jala.backend.storage.enums.StorageFolder;
import com.jala.backend.storage.service.StorageService;
import com.jala.backend.testsupport.WebSecurityTestConfig;
import com.jala.backend.testsupport.WebSliceTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StorageController.class)
@Import(WebSecurityTestConfig.class)
class StorageControllerTest extends WebSliceTestBase {

    @MockitoBean
    private StorageService storageService;

    private static final byte[] PNG =
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};

    @Test
    @DisplayName("anonymous upload is 401")
    void anon() throws Exception {
        mockMvc.perform(multipart("/api/v1/storage/upload")
                        .file(new MockMultipartFile("file", "a.png", "image/png", PNG))
                        .param("folder", "MEDICINE")
                        .param("entityId", "e1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("authenticated upload returns the stored URL")
    void upload_ok() throws Exception {
        given(storageService.upload(any(), any(StorageFolder.class), any(), any()))
                .willReturn("https://storage/medicine/e1/a.png");

        mockMvc.perform(multipart("/api/v1/storage/upload")
                        .file(new MockMultipartFile("file", "a.png", "image/png", PNG))
                        .param("folder", "MEDICINE")
                        .param("entityId", "e1"))
                .andExpect(status().isCreated());
    }
}
