package com.felipestanzani.migrationdemo.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler Integration Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Validation Error Handling")
    class ValidationErrorTests {

        @Test
        @DisplayName("should return 400 with validation errors for blank name")
        void shouldReturn400ForBlankName() throws Exception {
            String requestBody = "{\"name\":\"\",\"price\":10.0}";

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value(containsString("validation failed")))
                    .andExpect(jsonPath("$.instance").value("/api/v1/products"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                    .andExpect(jsonPath("$.validationErrors.name").exists());
        }

        @Test
        @DisplayName("should return 400 with validation errors for null price")
        void shouldReturn400ForNullPrice() throws Exception {
            String requestBody = "{\"name\":\"Product\",\"price\":null}";

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.price").exists());
        }

        @Test
        @DisplayName("should return 400 with validation errors for negative price")
        void shouldReturn400ForNegativePrice() throws Exception {
            String requestBody = "{\"name\":\"Product\",\"price\":-5.0}";

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.price").exists());
        }
    }

    @Nested
    @DisplayName("ProductNotFoundException Handling")
    class ProductNotFoundTests {

        @Test
        @DisplayName("should return 404 when product not found by id")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/products/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value(containsString("Product not found")))
                    .andExpect(jsonPath("$.detail").value(containsString(nonExistentId.toString())))
                    .andExpect(jsonPath("$.instance").value("/api/v1/products/" + nonExistentId))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()));
        }

        @Test
        @DisplayName("should return 404 for v2 endpoint when product not found")
        void shouldReturn404ForV2Endpoint() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v2/products/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Not Found"))
                    .andExpect(jsonPath("$.detail").value(containsString(nonExistentId.toString())));
        }
    }

    @Nested
    @DisplayName("Method Not Allowed Handling")
    class MethodNotAllowedTests {

        @Test
        @DisplayName("should return 405 for unsupported HTTP method")
        void shouldReturn405ForUnsupportedMethod() throws Exception {
            mockMvc.perform(delete("/api/v1/products"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.type").value("about:blank"))
                    .andExpect(jsonPath("$.title").value("Method Not Allowed"))
                    .andExpect(jsonPath("$.status").value(405))
                    .andExpect(jsonPath("$.instance").value("/api/v1/products"))
                    .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                    .andExpect(jsonPath("$.errors.method").value("DELETE"));
        }
    }

    @Nested
    @DisplayName("RFC 7807 Compliance")
    class RFC7807ComplianceTests {

        @Test
        @DisplayName("should return RFC 7807 Problem Details structure")
        void shouldReturnRFC7807Structure() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/products/" + nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.type").exists())
                    .andExpect(jsonPath("$.title").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.detail").exists())
                    .andExpect(jsonPath("$.instance").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
