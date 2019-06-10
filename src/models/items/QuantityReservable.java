package models.items;

import models.facilities.Rack;


/**
 * Interface definition for all quantity reservable classes.
 * <p>
 * A {@code QuantityReservable} class is a class that can accept reserving quantities.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}.
 *
 * @see Item
 * @see QuantityAddable
 */
public interface QuantityReservable<T> {

    /**
     * Reserves some units specified by the given {@code QuantityAddable} container.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see QuantityReservable#confirmReservation(QuantityAddable)
     */
    void reserve(QuantityAddable<T> container);

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container, and removes those reserved units from this object.
     * <p>
     * This function should be called after reserving a same or a super container first;
     * otherwise un-expected behaviour could occur.
     *
     * @param container the {@code QuantityAddable} container.
     *
     * @see QuantityReservable#reserve(QuantityAddable)
     */
    void confirmReservation(QuantityAddable<T> container);
}
