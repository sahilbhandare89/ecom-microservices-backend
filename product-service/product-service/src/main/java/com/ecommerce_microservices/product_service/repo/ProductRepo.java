package com.ecommerce_microservices.product_service.repo;

import com.ecommerce_microservices.product_service.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends MongoRepository<Product,String> {
}
