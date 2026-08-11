package com.ecommerce_microservices.product_service.controller;

import com.ecommerce_microservices.product_service.dto.ProductRequest;
import com.ecommerce_microservices.product_service.dto.ProductResponse;
import com.ecommerce_microservices.product_service.enu.Category;
import com.ecommerce_microservices.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;

    // -- Create a New Project ------------------------------------------------------------------------------------
    @PostMapping(
            value = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductResponse> createProduct(

            @RequestPart("product")
            @Valid ProductRequest request,

            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {

        ProductResponse response =
                productService.createProduct(request, images);

        log.info("product created successfully {}"+response);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // --- getAll products -----------------------------------------------------------------------------------------
    @GetMapping("/all")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> products =
                productService.getAllProducts();

        return ResponseEntity.ok(products);
    }

    // -- get Product By Id ----------------------------------------------------------------------------------------
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable String productId
    ) {

        ProductResponse product =
                productService.getProductById(productId);

        return ResponseEntity.ok(product);
    }

    // -- Search Product in ElesticSearch-----------------------------------------------------------------------------
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                productService.searchProducts(keyword)
        );
    }

    // -- Delete Product from Both ------------------------------------------------------------------------------------
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable String productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok("Product deleted successfully.");
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(
            @RequestBody List<String> productIds) {

        return ResponseEntity.ok(productService.getProductsByIds(productIds));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> filter(

            @RequestParam(required = false) String brand,

            @RequestParam(required = false) Category category,

            @RequestParam(required = false) BigDecimal minPrice,

            @RequestParam(required = false) BigDecimal maxPrice,

            @RequestParam(required = false) String keyword

    ) {

        return ResponseEntity.ok(
                productService.filter(
                        brand,
                        category,
                        minPrice,
                        maxPrice,
                        keyword
                )
        );
    }
}
