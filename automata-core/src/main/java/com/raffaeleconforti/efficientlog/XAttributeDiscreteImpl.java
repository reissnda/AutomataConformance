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

package com.raffaeleconforti.efficientlog;

import java.util.Objects;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeDiscrete;

/**
 * Created by Raffaele Conforti (conforti.raffaele@gmail.com) on 4/08/2016.
 */
public class XAttributeDiscreteImpl extends XAttributeImpl implements XAttributeDiscrete {

  /**
   *
   */
  private static final long serialVersionUID = 2209799959584107671L;

  /**
   * Value of the attribute.
   */
  private long value;

  /**
   * Creates a new instance.
   *
   * @param key
   *     The key of the attribute.
   * @param value
   *     Value of the attribute.
   */
  public XAttributeDiscreteImpl(String key, long value) {
    this(key, value, null);
  }

  /**
   * Creates a new instance.
   *
   * @param key
   *     The key of the attribute.
   * @param value
   *     Value of the attribute.
   * @param extension
   *     The extension of the attribute.
   */
  public XAttributeDiscreteImpl(String key, long value, XExtension extension) {
    super(key, extension);
    this.value = value;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.deckfour.xes.model.XAttributeDiscrete#getValue()
   */
  public long getValue() {
    return this.value;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.deckfour.xes.model.XAttributeDiscrete#setValue(long)
   */
  public void setValue(long value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return Long.toString(value);
  }

  public Object clone() {
    return super.clone();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof XAttributeDiscrete) { // compares types
      XAttributeDiscrete other = (XAttributeDiscrete) obj;
      return super.equals(other) // compares keys
          && (value == other.getValue()); // compares values
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
    if (!(other instanceof XAttributeDiscrete)) {
      throw new ClassCastException();
    }
    int result = super.compareTo(other);
    if (result != 0) {
      return result;
    }
    return ((Long) value).compareTo(((XAttributeDiscrete) other).getValue());
  }
}
