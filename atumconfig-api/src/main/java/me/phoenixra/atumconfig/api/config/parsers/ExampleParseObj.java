package me.phoenixra.atumconfig.api.config.parsers;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Example object for ConfigParser
 */
@AllArgsConstructor
@Data
public class ExampleParseObj {
    private String id;
    private boolean test;
    private int value;
}
