package com.bhagwat.scm.carrierService.common;

public enum WeightUOM {
    KILOGRAM("kg"),
    GRAM("g"),
    MILLIGRAM("mg"),
    TON("t"),
    POUND("lb"),
    OUNCE("oz");

    private final String symbol;

    WeightUOM(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}

