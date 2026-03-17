package com.felipestanzani.migrationdemo.controller;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.exception.ProductNotFoundException;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.service.interfaces.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ProductControllerV1.class)
@DisplayName("ProductControllerV1")
class ProductControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProductTests {

        @Test
        @DisplayName("should create product and return 201 with saved product")
        void shouldCreateProductAndReturn201() throws Exception {
            ProductRequest request = new ProductRequest("New Product", 15.99);
            Product savedProduct = new Product();
            savedProduct.setId(UUID.randomUUID());
            savedProduct.setName("New Product");
            savedProduct.setPrice(15.99);

            when(productService.save(any(ProductRequest.class))).thenReturn(savedProduct);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("New Product"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(15.99))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

            verify(productService).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should delegate to productService with correct request")
        void shouldDelegateToProductServiceWithCorrectRequest() throws Exception {
            ProductRequest request = new ProductRequest("Test Product", 99.99);
            Product savedProduct = new Product();
            savedProduct.setId(UUID.randomUUID());
            savedProduct.setName("Test Product");
            savedProduct.setPrice(99.99);

            when(productService.save(any(ProductRequest.class))).thenReturn(savedProduct);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isCreated());

            verify(productService).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has blank name")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            ProductRequest request = new ProductRequest("", 15.99);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has null price")
        void shouldReturn400WhenPriceIsNull() throws Exception {
            String requestBody = "{\"name\":\"Product\",\"price\":null}";

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has non-positive price")
        void shouldReturn400WhenPriceIsNonPositive() throws Exception {
            ProductRequest request = new ProductRequest("Product", -1.0);

            mockMvc.perform(post("/api/v1/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetProductsTests {

        @Test
        @DisplayName("should return 200 with list of product names")
        void shouldReturn200WithProductNames() throws Exception {
            List<String> names = List.of("Product A", "Product B");
            when(productService.findAllNames()).thenReturn(names);

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("Product A"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value("Product B"));

            verify(productService).findAllNames();
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            when(productService.findAllNames()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));

            verify(productService).findAllNames();
        }

        @Test
        @DisplayName("should delegate to productService findAllNames")
        void shouldDelegateToProductServiceFindAllNames() throws Exception {
            when(productService.findAllNames()).thenReturn(List.of("Single Product"));

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("Single Product"));

            verify(productService).findAllNames();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return 200 with product when product exists")
        void shouldReturn200WithProductWhenProductExists() throws Exception {
            UUID productId = UUID.randomUUID();
            Product product = new Product();
            product.setId(productId);
            product.setName("Test Product");
            product.setPrice(29.99);

            when(productService.findById(productId)).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/{id}", productId))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(productId.toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Product"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(29.99));

            verify(productService).findById(productId);
        }

        @Test
        @DisplayName("should return 404 when product does not exist")
        void shouldReturn404WhenProductDoesNotExist() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            when(productService.findById(nonExistentId))
                    .thenThrow(new ProductNotFoundException(nonExistentId));

            mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());

            verify(productService).findById(nonExistentId);
        }

        @Test
        @DisplayName("should delegate to productService findById with correct id")
        void shouldDelegateToProductServiceFindById() throws Exception {
            UUID productId = UUID.randomUUID();
            Product product = new Product();
            product.setId(productId);
            product.setName("Another Product");
            product.setPrice(99.99);

            when(productService.findById(productId)).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/{id}", productId))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(productService).findById(productId);
        }
    }
}
