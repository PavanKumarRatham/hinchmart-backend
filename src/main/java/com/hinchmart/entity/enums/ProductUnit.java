package com.hinchmart.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductUnit {
    PIECE("Piece"),
    BAG("Bag"),
    KG("Kg"),
    TON("Ton"),
    METER("Meter"),
    FEET("Feet"),
    LITER("Liter"),
    BOX("Box"),
    SET("Set"),
    BUNDLE("Bundle"),
    ROLL("Roll"),
    SQ_FT("Sq.Ft");

    private final String displayName;

    ProductUnit(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ProductUnit fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String clean = value.trim().replace(".", "").replace(" ", "_").toUpperCase();
        for (ProductUnit unit : ProductUnit.values()) {
            if (unit.name().equalsIgnoreCase(clean) || 
                unit.displayName.equalsIgnoreCase(value.trim()) ||
                unit.name().replace("_", "").equalsIgnoreCase(clean)) {
                return unit;
            }
        }
        return ProductUnit.valueOf(clean);
    }
}
