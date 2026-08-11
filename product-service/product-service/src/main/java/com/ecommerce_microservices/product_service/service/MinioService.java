package com.ecommerce_microservices.product_service.service;

import com.ecommerce_microservices.product_service.config.MinioProperties;
import com.ecommerce_microservices.product_service.entity.ProductImage;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public ProductImage uploadImage(MultipartFile file, boolean primaryImage) {

        try {

            String objectKey =
                    "products/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = properties.getEndpoint()
                    + "/"
                    + properties.getBucketName()
                    + "/"
                    + objectKey;

            return ProductImage.builder()
                    .objectKey(objectKey)
                    .url(url)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .primaryImage(primaryImage)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to MinIO", e);
        }
    }
}