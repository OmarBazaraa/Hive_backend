package models.items;

import models.facilities.Rack;


/**
 * Interface definition for all quantity reservable classes.
 * <p>
 * A {@code QuantityReservable} object is an object that can accept reserving quantities.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}.
 *
 * @see Item
 * @see QuantityAddable
 */
public interface QuantityReservable<T> {

    /**
     * Reserves some units specified by the given key-value pair.
     * Reservation can be confirmed or undone by passing negative quantities.
     * <p>
     * This function should be called after ensuring that reservation is possible.
     *
     * @param key      the key to reserve.
     * @param quantity the quantity to reserve.
     */
    void reserve(T key, int quantity);
}