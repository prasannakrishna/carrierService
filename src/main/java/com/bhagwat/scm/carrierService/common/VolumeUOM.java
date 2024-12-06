package com.bhagwat.scm.carrierService.common;

public enum VolumeUOM {
    CUBIC_METER("m³"),
    CUBIC_CENTIMETER("cm³"),
    LITER("L"),
    MILLILITER("mL"),
    CUBIC_FEET("ft³"),
    CUBIC_INCH("in³");

    private final String symbol;

    VolumeUOM(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

