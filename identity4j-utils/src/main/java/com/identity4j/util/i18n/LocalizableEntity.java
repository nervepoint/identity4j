/* HEADER */
package com.identity4j.util.i18n;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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