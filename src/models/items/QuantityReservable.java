package models.items;

import models.facilities.Rack;


/**
 * Interface definition for all quantity reservable classes.
 * <p>
 * An {@code QuantityReservable} class is a class that can accept reserving quantities.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}.
 *
 * @see QuantityAddable
 */
public interface QuantityReservable<T> {

    /**
     * Reserves some units specified by the given {@code QuantityAddable} container.
     *
     * @param container the {@code QuantityAddable} container.
     */
    void reserve(QuantityAddable<T> container) throws Exception;

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container, and removes those reserved units from this object.
     *
     * @param container the {@code QuantityAddable} container.
     */
    void confirmReservation(QuantityAddable<T> container) throws Exception;
}
