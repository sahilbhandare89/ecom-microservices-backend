package com.ecommerce_microservices.product_service.service;

import com.ecommerce_microservices.product_service.entity.ProductDocument;
import com.ecommerce_microservices.product_service.enu.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<ProductDocument> filterProducts(
            String brand,
            Category category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {

        Criteria criteria = new Criteria();

        if (brand != null && !brand.isBlank()) {
            criteria = criteria.and(
                    Criteria.where("productBrand").is(brand)
            );
        }

        if (category != null) {
            criteria = criteria.and(
                    Criteria.where("category").is(category.name())
            );
        }

        if (minPrice != null) {
            criteria = criteria.and(
                    Criteria.where("productPrice").greaterThanEqual(minPrice)
            );
        }

        if (maxPrice != null) {
            criteria = criteria.and(
                    Criteria.where("productPrice").lessThanEqual(maxPrice)
            );
        }

        if (keyword != null && !keyword.isBlank()) {

            Criteria keywordCriteria = new Criteria()
                    .or(
                            Criteria.where("productName").contains(keyword)
                    )
                    .or(
                            Criteria.where("productDescription").contains(keyword)
                    );

            criteria = criteria.and(keywordCriteria);
        }

        Query query = new CriteriaQuery(criteria);

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        return hits.stream()
                .map(SearchHit::getContent)
                .toList();
    }

}
