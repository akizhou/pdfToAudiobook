package com.enums;

public enum PageReturns {
    OK,          // Valid inputs were sent in
    WRONG_ORDER, // Used when values are correct but the ordering is wrong
    ILLEGAL_ARG, // When an illegal symbol is used
    EMPTY_ARG    // When a field is left empty
}
