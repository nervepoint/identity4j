package com.identity4j.util.i18n;

import java.io.Serializable;

public interface Identifiable<T extends Serializable> {
    T getId();
}