package models.items;

import models.facilities.Rack;


/**
 * Interface definition for all {@link Item} reservable classes.
 * <p>
 * An {@code ItemReservable} class is a class that can accept reserving items.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}.
 *
 * @see QuantityAddable
 */
public interface ItemReservable {

    /**
     * Reserves some units specified by the given {@code QuantityAddable} container.
     *
     * @param container the {@code QuantityAddable} container.
     */
    void reserve(QuantityAddable container) throws Exception;

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code QuantityAddable} container, and removes those reserved units from this object.
     *
     * @param container the {@code QuantityAddable} container.
     */
    void confirmReservation(QuantityAddable container) throws Exception;
}
