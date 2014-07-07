/* HEADER */
package com.identity4j.util.i18n;

/**
 * An extension of {@link Entity} that should be used for entities that may have an
 * icon when rendered. <br/>
 * This is the logical name of the icon, size is determined by renderer.
 */
public interface IconifiableEntity extends Entity {

    /**
     * Get the logical icon name
     * 
     * @return icon name
     */
    String getIconName();
}