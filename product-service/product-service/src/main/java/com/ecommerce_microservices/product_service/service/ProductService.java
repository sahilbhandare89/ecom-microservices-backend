package com.ecommerce_microservices.product_service.service;


import com.ecommerce_microservices.product_service.dto.ProductRequest;
import com.ecommerce_microservices.product_service.dto.ProductResponse;
import com.ecommerce_microservices.product_service.entity.Product;
import com.ecommerce_microservices.product_service.entity.ProductDocument;
import com.ecommerce_microservices.product_service.entity.ProductImage;
import com.ecommerce_microservices.product_service.enu.Category;
import com.ecommerce_microservices.product_service.mapper.ProductMapper;
import com.ecommerce_microservices.product_service.repo.ProductRepo;
import com.ecommerce_microservices.product_service.repo.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepo productRepo;
    private final MinioService minioService;
    private final ProductSearchRepository productSearchRepository;
    private final ProductSearchService productSearchService;
    private final ProductMapper mapper;


    // -- Create a New Project ------------------------------------------------------------------------------------

    public ProductResponse createProduct(ProductRequest request,
                                         List<MultipartFile> images) {

        List<ProductImage> imageList = new ArrayList<>();

        if (images != null && !images.isEmpty()) {

            for (int i = 0; i < images.size(); i++) {

                imageList.add(
                        minioService.uploadImage(images.get(i), i == 0)
                );
            }
        }

        Product product = Product.builder()
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .productPrice(request.getProductPrice())
                .category(request.getCategory())
                .productBrand(request.getProductBrand())
                .stockQuantity(request.getStockQuantity())
                .images(imageList)
                .build();

        Product savedProduct = productRepo.save(product);

        log.info("Saved product {}", savedProduct);

        try {
            ProductDocument document = mapper.toDocument(savedProduct);
            productSearchRepository.save(document);

            log.info("Saved product in elasticsearch  {}", savedProduct);
        } catch (Exception ex) {
            log.error("Failed to index product {}", savedProduct.getId(), ex);
        }

        return ProductResponse.builder()
                .id(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .productDescription(savedProduct.getProductDescription())
                .category(savedProduct.getCategory())
                .productBrand(savedProduct.getProductBrand())
                .productPrice(savedProduct.getProductPrice())
                .stockQuantity(savedProduct.getStockQuantity())
                .images(savedProduct.getImages())
                .build();
    }

    // --- getAll products ---------------------------------------------------------------------------------

    public List<ProductResponse> getAllProducts() {

        return productRepo.findAll()
                .stream()
                .map(product -> {

                    List<ProductImage> imageResponses =
                            product.getImages() == null
                                    ? List.of()
                                    : product.getImages()
                                    .stream()
                                    .map(image -> ProductImage.builder()
                                            .objectKey(image.getObjectKey())
                                            .url(image.getUrl())
                                            .fileName(image.getFileName())
                                            .contentType(image.getContentType())
                                            .primaryImage(image.getPrimaryImage())
                                            .size(image.getSize())
                                            .build())
                                    .toList();

                    return ProductResponse.builder()
                            .id(product.getId())
                            .productName(product.getProductName())
                            .productDescription(product.getProductDescription())
                            .productPrice(product.getProductPrice())
                            .productBrand(product.getProductBrand())
                            .category(product.getCategory())      // <-- FIX
                            .stockQuantity(product.getStockQuantity())
                            .images(imageResponses)
                            .build();
                })
                .toList();
    }

    // -- get Product By Id -----------------------------------------------------------------------------------

    public ProductResponse getProductById(String productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with id: " + productId));

        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productPrice(product.getProductPrice())
                .productBrand(product.getProductBrand())
                .category(product.getCategory())      // <-- FIX
                .stockQuantity(product.getStockQuantity())
                .images(product.getImages() == null ? List.of() : product.getImages())
                .build();
    }


    // -- Search Product in ElesticSearch------------------------------------------------------------------------
    public List<ProductResponse> searchProducts(String keyword) {

        List<ProductDocument> documents =
                productSearchRepository.findByProductNameContaining(keyword);

        return documents.stream()
                .map(document -> ProductResponse.builder()
                        .id(document.getId())
                        .productName(document.getProductName())
                        .productDescription(document.getProductDescription())
                        .productPrice(document.getProductPrice())
                        .productBrand(document.getProductBrand())
                        .category(document.getCategory())
                        .stockQuantity(document.getStockQuantity())
                        .build())
                .toList();
    }

    // -- Delete Product from Both ----------------------------------------------------------------------------------
    public void deleteProduct(String productId) {

        Product product = productRepo.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with id: " + productId));

        // Delete from MongoDB
        productRepo.delete(product);

        log.info("Deleted product from the database {}", product);

        // Delete from Elasticsearch
        productSearchRepository.deleteById(productId);

        log.info("Deleted product from the ElesticSearch {}", product);
    }


    public List<ProductResponse> getProductsByIds(List<String> ids) {

        return productRepo.findAllById(ids)
                .stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .productDescription(product.getProductDescription())
                        .productPrice(product.getProductPrice())
                        .productBrand(product.getProductBrand())
                        .category(product.getCategory())
                        .stockQuantity(product.getStockQuantity())
                        .images(product.getImages())
                        .build())
                .toList();
    }


    public List<ProductResponse> filter(
            String brand,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {

        return productSearchService
                .filterProducts(
                        brand,
                        category,
                        minPrice,
                        maxPrice,
                        keyword
                )
                .stream()
                .map(document -> ProductResponse.builder()
                        .id(document.getId())
                        .productName(document.getProductName())
                        .productDescription(document.getProductDescription())
                        .productPrice(document.getProductPrice())
                        .category(document.getCategory())
                        .productBrand(document.getProductBrand())
                        .stockQuantity(document.getStockQuantity())
                        .build())
                .toList();

    }
}
