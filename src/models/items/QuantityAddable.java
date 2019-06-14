package models.items;

import models.facilities.Rack;
import models.tasks.Order;
import models.tasks.Task;

import java.util.Map;


/**
 * Interface definition for all quantity addable classes.
 * <p>
 * A {@code QuantityAddable} class is a class that can accept adding and removing quantities.
 * <p>
 * This interface is to be implemented by {@link Rack}, {@link Item}, {@link Order}, {@link Task}.
 *
 * @see Item
 * @see QuantityReservable
 */
public interface QuantityAddable<T> extends Iterable<Map.Entry<T, Integer>> {

    /**
     * Returns the first key-value pair in this object.
     *
     * @return the first key-value pair.
     */
    default Map.Entry<T, Integer> getFirst() {
        return iterator().next();
    }

    /**
     * Returns the current quantity of a key of type {@link T} in this object.
     *
     * @param key the key to get its quantity.
     *
     * @return the quantity of the given key.
     */
    int get(T key);

    /**
     * Updates the quantity of a key of type {@link T} in this object.
     *
     * @param key      the key to be updated.
     * @param quantity the quantity to be updated with.
     */
    void add(T key, int quantity);

    /**
     * Updates the quantity of a key in this object
     * <p>
     * This function is considered the default implementation of the
     * {@link QuantityAddable#add(Object, int)} method.
     *
     * @param map      the map of key-value pairs to be updated.
     * @param key      the key to be updated.
     * @param quantity the quantity to be updated with.
     */
    static <T> void update(Map<T, Integer> map, T key, int quantity) {
        int total = quantity + map.getOrDefault(key, 0);

        if (total != 0) {
            map.put(key, total);
        } else {
            map.remove(key);
        }
    }
}
