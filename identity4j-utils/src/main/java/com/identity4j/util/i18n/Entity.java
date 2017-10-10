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