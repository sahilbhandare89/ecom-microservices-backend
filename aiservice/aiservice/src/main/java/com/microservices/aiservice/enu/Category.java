package com.microservices.aiservice.enu;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Category {

    MOBILE,
    LAPTOP,
    TABLET,
    SMARTWATCH,
    HEADPHONES,
    CAMERA,
    TELEVISION,
    GAMING,
    ELECTRONICS,
    ACCESSORIES,
    HOME_AND_KITCHEN,
    FASHION,
    FOOTWEAR,
    BOOKS,
    BEAUTY,
    HEALTH,
    SPORTS,
    TOYS,
    GROCERY,
    AUTOMOTIVE,
    PET_SUPPLIES,
    JEWELRY,
    FURNITURE,
    STATIONERY,
    BABY_PRODUCTS;

    @JsonCreator
    public static Category from(String value) {

        if (value == null) {
            return null;
        }

        return Category.valueOf(value.trim().toUpperCase());
    }
}