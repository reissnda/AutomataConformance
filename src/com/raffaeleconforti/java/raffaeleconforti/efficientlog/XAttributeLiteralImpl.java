/*
 *  Copyright (C) 2018 Raffaele Conforti (www.raffaeleconforti.com)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.raffaeleconforti.java.raffaeleconforti.efficientlog;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;

import java.util.Objects;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeLiteralImpl extends XAttributeImpl implements
        XAttributeLiteral {

    /**
     *
     */
    private static final long serialVersionUID = -1844032762689490775L;

    /**
     * Value of the attribute.
     */
    private String value;

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param value
     *            Value of the attribute.
     */
    public XAttributeLiteralImpl(String key, String value) {
        this(key, value, null);
    }

    /**
     * Creates a new instance.
     *
     * @param key
     *            The key of the attribute.
     * @param value
     *            Value of the attribute.
     * @param extension
     *            The extension of the attribute.
     */
    public XAttributeLiteralImpl(String key, String value, XExtension extension) {
        super(key, extension);
        setValue(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeLiteral#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deckfour.xes.model.XAttributeLiteral#setValue(java.lang.String)
     */
    public void setValue(String value) {
        //#251 An empty trimmed string should not be treated as a null value.
        if (value == null) { //#251 || value.trim().length() == 0) {
            throw new NullPointerException(
                    "No null value allowed in literal attribute!");
        }
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.value;
    }

    public Object clone() {
        XAttributeLiteralImpl clone = (XAttributeLiteralImpl) super.clone();
        clone.value = new String(this.value);
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof XAttributeLiteral) { // compares types
            XAttributeLiteral other = (XAttributeLiteral) obj;
            return super.equals(other) // compares keys
                    && value.equals(other.getValue()); // compares values
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(getKey(), value);
    }

    @Override
    public int compareTo(XAttribute other) {
        if (!(other instanceof XAttributeLiteral)) {
            throw new ClassCastException();
        }
        int result = super.compareTo(other);
        if (result != 0) {
            return result;
        }
        return value.compareTo(((XAttributeLiteral)other).getValue());
    }
}

