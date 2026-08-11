package com.ecommerce_microservices.product_service.repo;

import com.ecommerce_microservices.product_service.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument,String> {

    List<ProductDocument> findByProductNameContaining(String keyword);
}
