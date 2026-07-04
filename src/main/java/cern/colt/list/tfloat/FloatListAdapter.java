/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.colt.list.tfloat;

/**
 * Adapter that permits an {@link cern.colt.list.tfloat.AbstractFloatList} to be
 * viewed and treated as a JDK 1.2 {@link java.util.AbstractList}. Makes the
 * contained list compatible with the JDK 1.2 Collections Framework.
 * <p>
 * Any attempt to pass elements other than <code>java.lang.Number</code> to setter
 * methods will throw a <code>java.lang.ClassCastException</code>.
 * <code>java.lang.Number.floatValue()</code> is used to convert objects into
 * primitive values which are then stored in the backing templated list. Getter
 * methods return <code>java.lang.Float</code> objects.
 */
public class FloatListAdapter extends java.util.AbstractList implements java.util.List {
    protected AbstractFloatList content;

    /**
     * Constructs a list backed by the specified content list.
     * @param content
     */
    public FloatListAdapter(AbstractFloatList content) {
        this.content = content;
    }

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation). Shifts the element currently at that position (if
     * any) and any subsequent elements to the right (adds one to their
     * indexes).
     * <p>
     * 
     * @param index
     *            index at which the specified element is to be inserted.
     * @param element
     *            element to be inserted.
     * 
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this list.
     * @throws IllegalArgumentException
     *             if some aspect of the specified element prevents it from
     *             being added to this list.
     * @throws IndexOutOfBoundsException
     *             index is out of range (<code>index &lt;
     * 		  0 || index &gt; size()</code>).
     */

    public void add(int index, Object element) {
        content.beforeInsert(index, value(element));
        modCount++;
    }

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            index of element to return.
     * 
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException
     *             if the given index is out of range (
     *             <code>index &lt; 0 || index &gt;= size()</code>).
     */

    public Object get(int index) {
        return object(content.get(index));
    }

    /**
     * Transforms an element of a primitive data type to an object.
     * @param element
     * @return 
     */
    protected static Object object(float element) {
        return element;
    }

    /**
     * Removes the element at the specified position in this list (optional
     * operation). Shifts any subsequent elements to the left (subtracts one
     * from their indexes). Returns the element that was removed from the list.
     * <p>
     * 
     * @param index
     *            the index of the element to remove.
     * @return the element previously at the specified position.
     * 
     * @throws IndexOutOfBoundsException
     *             if the specified index is out of range (
     *             <code>index &lt; 0 || index &gt;= size()</code>).
     */

    public Object remove(int index) {
        Object old = get(index);
        content.remove(index);
        modCount++;
        return old;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     * <p>
     * 
     * @param index
     *            index of element to replace.
     * @param element
     *            element to be stored at the specified position.
     * @return the element previously at the specified position.
     * 
     * @throws ClassCastException
     *             if the class of the specified element prevents it from being
     *             added to this list.
     * @throws IllegalArgumentException
     *             if some aspect of the specified element prevents it from
     *             being added to this list.
     * 
     * @throws IndexOutOfBoundsException
     *             if the specified index is out of range (
     *             <code>index &lt; 0 || index &gt;= size()</code>).
     */

    public Object set(int index, Object element) {
        Object old = get(index);
        content.set(index, value(element));
        return old;
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list.
     */

    public int size() {
        return content.size();
    }

    /**
     * Transforms an object element to a primitive data type.
     * @param element
     * @return 
     */
    protected static float value(Object element) {
        return ((Number) element).floatValue();
    }
}
