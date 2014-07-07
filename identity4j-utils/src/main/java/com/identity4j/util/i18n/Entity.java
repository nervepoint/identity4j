/* HEADER */
package com.identity4j.util.i18n;

/**
 * Interface represent the concept of an <i>Entity</i>. An entity is any object
 * that may be rendered by some UI component.
 */
public interface Entity extends Identifiable<String> {

    /**
     * Get the unique identifier for this entity. The Entity ID need only be
     * unique in the context it is going to be used in.
     * 
     * @return entity ID
     */
    String getId();
}