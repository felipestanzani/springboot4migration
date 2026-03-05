package com.felipestanzani.migrationdemo.service;

import com.felipestanzani.migrationdemo.dto.ProductRequest;
import com.felipestanzani.migrationdemo.dto.ProductResponse;
import com.felipestanzani.migrationdemo.exception.ForcedFallbackException;
import com.felipestanzani.migrationdemo.model.Product;
import com.felipestanzani.migrationdemo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Product A");
        product1.setPrice(10.0);

        product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Product B");
        product2.setPrice(20.0);
    }

    @Nested
    @DisplayName("save")
    class SaveTests {

        @Test
        @DisplayName("should save product with request data and return saved product")
        void shouldSaveProductWithRequestData() {
            ProductRequest request = new ProductRequest("New Product", 15.99);
            when(repository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(UUID.randomUUID());
                return p;
            });

            Product result = productService.save(request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Product");
            assertThat(result.getPrice()).isEqualTo(15.99);
            verify(repository).save(any(Product.class));
        }

        @Test
        @DisplayName("should pass correct product to repository")
        void shouldPassCorrectProductToRepository() {
            ProductRequest request = new ProductRequest("Test Product", 99.99);
            Product savedProduct = new Product();
            savedProduct.setId(UUID.randomUUID());
            savedProduct.setName("Test Product");
            savedProduct.setPrice(99.99);
            when(repository.save(any(Product.class))).thenReturn(savedProduct);

            Product result = productService.save(request);

            assertThat(result).isSameAs(savedProduct);
        }
    }

    @Nested
    @DisplayName("findAllNames")
    class FindAllNamesTests {

        @RepeatedTest(50)
        @DisplayName("should return product names from repository or throw ForcedFallbackException")
        void shouldReturnProductNamesOrThrow() {
            lenient().when(repository.findAll()).thenReturn(List.of(product1, product2));

            try {
                List<String> result = productService.findAllNames();
                assertThat(result).containsExactly("Product A", "Product B");
                verify(repository).findAll();
            } catch (ForcedFallbackException e) {
                assertThat(e).hasMessage("It is not frozen, it is in panic!!!");
                verify(repository, never()).findAll();
            }
        }

        @RepeatedTest(50)
        @DisplayName("should return empty list or throw when repository has no products")
        void shouldReturnEmptyListOrThrowWhenNoProducts() {
            lenient().when(repository.findAll()).thenReturn(List.of());

            try {
                List<String> result = productService.findAllNames();
                assertThat(result).isEmpty();
                verify(repository).findAll();
            } catch (ForcedFallbackException e) {
                assertThat(e).hasMessage("It is not frozen, it is in panic!!!");
                verify(repository, never()).findAll();
            }
        }
    }

    @Nested
    @DisplayName("fallbackFindAllNames")
    class FallbackFindAllNamesTests {

        @Test
        @DisplayName("should return fallback list for any exception")
        void shouldReturnFallbackList() {
            List<String> result = productService.fallbackFindAllNames(new RuntimeException("Generic error"));

            assertThat(result).containsExactly("Charuteira", "Infundibuliar");
        }

        @Test
        @DisplayName("should return fallback list for ForcedFallbackException")
        void shouldReturnFallbackListForForcedFallbackException() {
            ForcedFallbackException exception = new ForcedFallbackException("It is not frozen, it is in panic!!!");

            List<String> result = productService.fallbackFindAllNames(exception);

            assertThat(result).containsExactly("Charuteira", "Infundibuliar");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("should return ProductResponse list from repository")
        void shouldReturnProductResponseList() {
            when(repository.findAll()).thenReturn(List.of(product1, product2));

            List<ProductResponse> result = productService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0)).satisfies(r -> {
                assertThat(r.name()).isEqualTo("Product A");
                assertThat(r.price()).isEqualTo(10.0);
            });
            assertThat(result.get(1)).satisfies(r -> {
                assertThat(r.name()).isEqualTo("Product B");
                assertThat(r.price()).isEqualTo(20.0);
            });
            verify(repository).findAll();
        }

        @Test
        @DisplayName("should return empty list when repository has no products")
        void shouldReturnEmptyListWhenNoProducts() {
            when(repository.findAll()).thenReturn(List.of());

            List<ProductResponse> result = productService.findAll();

            assertThat(result).isEmpty();
        }
    }
}
