package com.example;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The data wrapper of partial buffered res
 */
@Data
@AllArgsConstructor
public class PartialRes {
    private final byte[] buffer;
}
