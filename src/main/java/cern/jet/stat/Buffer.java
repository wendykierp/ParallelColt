/*
Copyright (C) 1999 CERN - European Organization for Nuclear Research.
Permission to use, copy, modify, distribute and sell this software and its documentation for any purpose 
is hereby granted without fee, provided that the above copyright notice appear in all copies and 
that both that copyright notice and this permission notice appear in supporting documentation. 
CERN makes no representations about the suitability of this software for any purpose. 
It is provided "as is" without expressed or implied warranty.
 */
package cern.jet.stat;

/**
 * A buffer holding elements; internally used for computing approximate
 * quantiles.
 */
public abstract class Buffer extends cern.colt.PersistentObject {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public int weight;

    public int level;

    public int k;

    public boolean isAllocated;

    /**
     * This method was created in VisualAge.
     * 
     * @param k
     *            int
     */
    public Buffer(int k) {
        this.k = k;
        this.weight = 1;
        this.level = 0;
        this.isAllocated = false;
    }

    /**
     * Clears the receiver.
     */
    public abstract void clear();

    /**
     * Returns whether the receiver is already allocated.
     * @return 
     */
    public boolean isAllocated() {
        return isAllocated;
    }

    /**
     * Returns whether the receiver is empty.
     * @return 
     */
    public abstract boolean isEmpty();

    /**
     * Returns whether the receiver is empty.
     * @return 
     */
    public abstract boolean isFull();

    /**
     * Returns whether the receiver is partial.
     * @return 
     */
    public boolean isPartial() {
        return !(isEmpty() || isFull());
    }

    /**
     * Returns whether the receiver's level.
     * @return 
     */
    public int level() {
        return level;
    }

    /**
     * Sets the receiver's level.
     * @param level
     */
    public void level(int level) {
        this.level = level;
    }

    /**
     * Returns the number of elements contained in the receiver.
     * @return 
     */
    public abstract int size();

    /**
     * Sorts the receiver.
     */
    public abstract void sort();

    /**
     * Returns whether the receiver's weight.
     * @return 
     */
    public int weight() {
        return weight;
    }

    /**
     * Sets the receiver's weight.
     * @param weight
     */
    public void weight(int weight) {
        this.weight = weight;
    }
}
