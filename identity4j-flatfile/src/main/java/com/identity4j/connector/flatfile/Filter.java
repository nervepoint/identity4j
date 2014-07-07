package com.identity4j.connector.flatfile;

import java.util.List;

public interface Filter {
    boolean include(List<String> row);
}
