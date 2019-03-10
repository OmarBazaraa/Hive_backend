package models.items;

import models.facilities.Rack;
import models.orders.Order;
import models.tasks.Task;

import java.util.Map;


/**
 * Interface definition for all {@link Item} addable classes.
 * <p>
 * An {@code ItemAddable} class is a class that can accept adding and removing items.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Order}, {@link Task}.
 *
 * @see ItemReservable
 */
public interface ItemAddable extends Iterable<Map.Entry<Item, Integer>> {


    /**
     * Returns the current quantity of an {@code Item} in this object.
     *
     * @param item the {@code Item} to get its quantity.
     *
     * @return the quantity of the given {@code Item}.
     */
    int getQuantity(Item item);

    /**
     * Updates the quantity of an {@code Item} in this object.
     * <p>
     * This function is used to add extra units of the given {@code Item} if the given
     * quantity is positive,
     * and used to remove existing units if the given quantity is negative.
     *
     * @param item     the {@code Item} to be updated.
     * @param quantity the quantity to be updated with.
     */
    void addItem(Item item, int quantity);
}
