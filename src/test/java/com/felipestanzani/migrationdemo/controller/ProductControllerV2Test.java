package com.felipestanzani.migrationdemo.controller;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
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

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ProductControllerV2.class)
@DisplayName("ProductControllerV2")
class ProductControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("POST /api/v2/products")
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

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("New Product"))
                    .andExpect(jsonPath("$.price").value(15.99))
                    .andExpect(jsonPath("$.id").exists());

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

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(productService).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has blank name")
        void shouldReturn400WhenNameIsBlank() throws Exception {
            ProductRequest request = new ProductRequest("", 15.99);

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has null price")
        void shouldReturn400WhenPriceIsNull() throws Exception {
            String requestBody = "{\"name\":\"Product\",\"price\":null}";

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }

        @Test
        @DisplayName("should return 400 when request has non-positive price")
        void shouldReturn400WhenPriceIsNonPositive() throws Exception {
            ProductRequest request = new ProductRequest("Product", -1.0);

            mockMvc.perform(post("/api/v2/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(productService, never()).save(any(ProductRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/products")
    class GetProductsTests {

        @Test
        @DisplayName("should return 200 with list of product responses")
        void shouldReturn200WithProductResponses() throws Exception {
            List<ProductResponse> responses = List.of(
                    new ProductResponse("Product A", 10.99),
                    new ProductResponse("Product B", 25.50)
            );
            when(productService.findAll()).thenReturn(responses);

            mockMvc.perform(get("/api/v2/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Product A"))
                    .andExpect(jsonPath("$[0].price").value(10.99))
                    .andExpect(jsonPath("$[1].name").value("Product B"))
                    .andExpect(jsonPath("$[1].price").value(25.50));

            verify(productService).findAll();
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            when(productService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v2/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(productService).findAll();
        }

        @Test
        @DisplayName("should delegate to productService findAll")
        void shouldDelegateToProductServiceFindAll() throws Exception {
            ProductResponse response = new ProductResponse("Single Product", 42.0);
            when(productService.findAll()).thenReturn(List.of(response));

            mockMvc.perform(get("/api/v2/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Single Product"))
                    .andExpect(jsonPath("$[0].price").value(42.0));

            verify(productService).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/v2/products/{id}")
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

            mockMvc.perform(get("/api/v2/products/{id}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId.toString()))
                    .andExpect(jsonPath("$.name").value("Test Product"))
                    .andExpect(jsonPath("$.price").value(29.99));

            verify(productService).findById(productId);
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

            mockMvc.perform(get("/api/v2/products/{id}", productId))
                    .andExpect(status().isOk());

            verify(productService).findById(productId);
        }
    }
}
