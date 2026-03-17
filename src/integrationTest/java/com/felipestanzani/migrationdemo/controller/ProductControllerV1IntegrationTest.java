package com.felipestanzani.migrationdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductControllerV1 Integration Tests")
class ProductControllerV1IntegrationTest {

        private static final String PRODUCTS_ENDPOINT = "/api/v1/products";
        private static final String NAME_PATH = "$.name";
        
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private ProductRepository productRepository;

        @BeforeEach
        void setUp() {
                productRepository.deleteAll();
        }

        @Nested
        @Transactional
        @DisplayName("POST /api/v1/products")
        class CreateProductTests {

                @Test
                @DisplayName("should create product successfully and persist to database")
                void shouldCreateProductSuccessfully() throws Exception {
                        ProductRequest request = new ProductRequest("Laptop", 1299.99);

                        String responseBody = mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andExpect(jsonPath("$.id").exists())
                                        .andExpect(jsonPath(NAME_PATH).value("Laptop"))
                                        .andExpect(jsonPath("$.price").value(1299.99))
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        Product savedProduct = objectMapper.readValue(responseBody, Product.class);

                        assertThat(productRepository.findById(savedProduct.getId())).isPresent();
                        Product dbProduct = productRepository.findById(savedProduct.getId()).get();
                        assertThat(dbProduct.getName()).isEqualTo("Laptop");
                        assertThat(dbProduct.getPrice()).isEqualTo(1299.99);
                }

                @Test
                @DisplayName("should create multiple products independently")
                void shouldCreateMultipleProducts() throws Exception {
                        ProductRequest request1 = new ProductRequest("Mouse", 29.99);
                        ProductRequest request2 = new ProductRequest("Keyboard", 79.99);

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request1)))
                                        .andExpect(status().isCreated());

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request2)))
                                        .andExpect(status().isCreated());

                        assertThat(productRepository.count()).isEqualTo(2);
                }

                @Test
                @DisplayName("should return 400 for invalid request with blank name")
                void shouldRejectBlankName() throws Exception {
                        ProductRequest request = new ProductRequest("", 99.99);

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        assertThat(productRepository.count()).isZero();
                }

                @Test
                @DisplayName("should return 400 for invalid request with null price")
                void shouldRejectNullPrice() throws Exception {
                        String requestBody = "{\"name\":\"Product\",\"price\":null}";

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andExpect(status().isBadRequest());

                        assertThat(productRepository.count()).isZero();
                }

                @Test
                @DisplayName("should return 400 for invalid request with negative price")
                void shouldRejectNegativePrice() throws Exception {
                        ProductRequest request = new ProductRequest("Product", -10.0);

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        assertThat(productRepository.count()).isZero();
                }

                @Test
                @DisplayName("should return 400 for invalid request with zero price")
                void shouldRejectZeroPrice() throws Exception {
                        ProductRequest request = new ProductRequest("Product", 0.0);

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isBadRequest());

                        assertThat(productRepository.count()).isZero();
                }
        }

        @Nested
        @Transactional
        @DisplayName("GET /api/v1/products")
        class GetProductsTests {

                @Test
                @DisplayName("should return list of product names from database")
                void shouldReturnProductNames() throws Exception {
                        Product product1 = new Product();
                        product1.setName("Tablet");
                        product1.setPrice(499.99);
                        productRepository.save(product1);

                        Product product2 = new Product();
                        product2.setName("Monitor");
                        product2.setPrice(349.99);
                        productRepository.save(product2);

                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(2)))
                                        .andExpect(jsonPath("$", containsInAnyOrder("Tablet", "Monitor")));
                }

                @Test
                @DisplayName("should return empty list when no products exist")
                void shouldReturnEmptyListWhenNoProducts() throws Exception {
                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(0)))
                                        .andExpect(jsonPath("$", empty()));
                }

                @Test
                @DisplayName("should return only product names without other details")
                void shouldReturnOnlyNames() throws Exception {
                        Product product = new Product();
                        product.setName("Headphones");
                        product.setPrice(149.99);
                        productRepository.save(product);

                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$[0]").value("Headphones"))
                                        .andExpect(jsonPath("$[0]").isString());
                }
        }

        @Nested
        @Transactional
        @DisplayName("GET /api/v1/products/{id}")
        class GetProductByIdTests {

                @Test
                @DisplayName("should return product by id from database")
                void shouldReturnProductById() throws Exception {
                        Product product = new Product();
                        product.setName("Smartphone");
                        product.setPrice(899.99);
                        Product savedProduct = productRepository.save(product);

                        mockMvc.perform(get("/api/v1/products/{id}", savedProduct.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(savedProduct.getId().toString()))
                                        .andExpect(jsonPath(NAME_PATH).value("Smartphone"))
                                        .andExpect(jsonPath("$.price").value(899.99));
                }

                @Test
                @DisplayName("should return 404 when product does not exist")
                void shouldReturn404ForNonExistentProduct() throws Exception {
                        UUID nonExistentId = UUID.randomUUID();

                        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.detail")
                                                        .value("Product not found with id: " + nonExistentId));
                }

                @Test
                @DisplayName("should return correct product when multiple products exist")
                void shouldReturnCorrectProductAmongMany() throws Exception {
                        Product product1 = new Product();
                        product1.setName("Product 1");
                        product1.setPrice(10.0);
                        productRepository.save(product1);

                        Product product2 = new Product();
                        product2.setName("Product 2");
                        product2.setPrice(20.0);
                        Product savedProduct2 = productRepository.save(product2);

                        Product product3 = new Product();
                        product3.setName("Product 3");
                        product3.setPrice(30.0);
                        productRepository.save(product3);

                        mockMvc.perform(get("/api/v1/products/{id}", savedProduct2.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(savedProduct2.getId().toString()))
                                        .andExpect(jsonPath(NAME_PATH).value("Product 2"))
                                        .andExpect(jsonPath("$.price").value(20.0));
                }
        }

        @Nested
        @Transactional
        @DisplayName("Integration Scenarios")
        class IntegrationScenariosTests {

                @Test
                @DisplayName("should create product and retrieve it by id")
                void shouldCreateAndRetrieveProduct() throws Exception {
                        ProductRequest request = new ProductRequest("Camera", 599.99);

                        String createResponse = mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        Product createdProduct = objectMapper.readValue(createResponse, Product.class);

                        mockMvc.perform(get("/api/v1/products/{id}", createdProduct.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.id").value(createdProduct.getId().toString()))
                                        .andExpect(jsonPath(NAME_PATH).value("Camera"))
                                        .andExpect(jsonPath("$.price").value(599.99));
                }

                @Test
                @DisplayName("should create product and see it in the products list")
                void shouldCreateAndSeeInList() throws Exception {
                        ProductRequest request = new ProductRequest("Speaker", 199.99);

                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());

                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(1)))
                                        .andExpect(jsonPath("$[0]").value("Speaker"));
                }

                @Test
                @DisplayName("should handle full product lifecycle")
                void shouldHandleFullLifecycle() throws Exception {
                        assertThat(productRepository.count()).isZero();

                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(0)));

                        ProductRequest request1 = new ProductRequest("Product A", 100.0);
                        String response1 = mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request1)))
                                        .andExpect(status().isCreated())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString();

                        Product product1 = objectMapper.readValue(response1, Product.class);

                        ProductRequest request2 = new ProductRequest("Product B", 200.0);
                        mockMvc.perform(post(PRODUCTS_ENDPOINT)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request2)))
                                        .andExpect(status().isCreated());

                        mockMvc.perform(get(PRODUCTS_ENDPOINT))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$", hasSize(2)))
                                        .andExpect(jsonPath("$", containsInAnyOrder("Product A", "Product B")));

                        mockMvc.perform(get("/api/v1/products/{id}", product1.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath(NAME_PATH).value("Product A"))
                                        .andExpect(jsonPath("$.price").value(100.0));

                        assertThat(productRepository.count()).isEqualTo(2);
                }
        }
}
