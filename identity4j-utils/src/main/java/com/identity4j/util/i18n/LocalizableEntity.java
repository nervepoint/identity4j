/* HEADER */
package com.identity4j.util.i18n;

/**
 * An extension of {@link Entity} that should be used for entities that may be
 * localised when rendered. <br/>
 * Whatever component is rendering the entity would normally make use of a
 * string pattern such as <code>[id].label</code> or <code>[id].toolTip</code>. <br/>
 * The localised messages should be retrieved using {@link Messages}, which
 * requires the bundle name.
 */
public interface LocalizableEntity extends Entity {

    /**
     * Get the bundle name for the entity
     * 
     * @return the bundle name
     */
    String getBundleName();
}