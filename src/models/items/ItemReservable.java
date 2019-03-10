package models.items;

import models.facilities.Rack;


/**
 * Interface definition for all {@link Item} reservable classes.
 * <p>
 * An {@code ItemReservable} class is a class that can accept reserving items.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}.
 *
 * @see ItemAddable
 */
public interface ItemReservable {

    /**
     * Reserves some units specified by the given {@code ItemAddable} container.
     *
     * @param container the {@code ItemAddable} container.
     */
    void reserve(ItemAddable container) throws Exception;

    /**
     * Confirms the previously assigned reservations specified by the given
     * {@code ItemAddable} container, and removes those reserved units from this object.
     *
     * @param container the {@code ItemAddable} container.
     */
    void confirmReservation(ItemAddable container) throws Exception;
}
