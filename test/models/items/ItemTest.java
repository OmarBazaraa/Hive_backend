package models.items;

import models.facilities.Rack;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ItemTest {

    //
    // Items distribution
    //

    // item1 -> 13 units
    // item1 -> { rack1: 5, rack2: 1, rack3: 7 }

    // item2 -> 9 units
    // item2 -> { rack1: 3, rack2: 6 }

    // item3 -> 7 units
    // item3 -> { rack2: 7 }

    //
    // Racks distribution
    //

    // rack1 -> { item1: 5, item2: 2 }
    // rack2 -> { item1: 1, item2: 6, item3: 7 }
    // rack3 -> { item1: 7 }
    Item item1, item2, item3;
    Rack rack1, rack2, rack3;

    /**
     * Initializes the required objects.
     */
    @Before
    public void before() {
        // Create new items
        item1 = new Item(1, 5);
        item2 = new Item(2, 7);
        item3 = new Item(3, 16);

        // Create new identical racks
        rack1 = new Rack(1, 100, 0);
        rack2 = new Rack(2, 100, 0);
        rack3 = new Rack(3, 100, 0);

        //
        // Add some units into the racks
        //
        rack1.add(item1, 5); // Rack 1
        rack1.add(item2, 3);
        rack2.add(item1, 1); // Rack 2
        rack2.add(item2, 6);
        rack2.add(item3, 7);
        rack3.add(item1, 7); // Rack 3
    }

    @Test
    public void propertyTest() {
        Item item = new Item(1, 5);

        // Check item properties
        Assert.assertEquals(item.getId(), 1);
        Assert.assertEquals(item.getWeight(), 5);
        Assert.assertEquals(item.getReservedUnits(), 0);
        Assert.assertEquals(item.getAvailableUnits(), 0);
        Assert.assertEquals(item.getTotalUnits(), 0);

        // Check simple units addition
        // Note that it is advised not to add items directly to item object
        // Items are better be added into a rack and the count will be synchronized internally
        Rack rack = new Rack(1, 100, 50);
        item.add(rack, 7);
        Assert.assertEquals(item.getTotalUnits(), 7);

        // Check simple units reservation
        item.reserve(-5);
        Assert.assertEquals(item.getReservedUnits(), -5);
        Assert.assertEquals(item.getAvailableUnits(), 12);
    }

    @Test
    public void countSyncTest() {
        // Check count synchronization
        Assert.assertEquals(item1.get(rack1), 5);
        Assert.assertEquals(item1.get(rack2), 1);
        Assert.assertEquals(item1.get(rack3), 7);
        Assert.assertEquals(item2.get(rack1), 3);
        Assert.assertEquals(item2.get(rack2), 6);
        Assert.assertEquals(item3.get(rack2), 7);

        // Check weights of the racks
        Assert.assertEquals(rack1.getStoredWeight(), 5 * item1.getWeight() + 3 * item2.getWeight());
        Assert.assertEquals(rack2.getStoredWeight(), 1 * item1.getWeight() + 6 * item2.getWeight() + 7 * item3.getWeight());
        Assert.assertEquals(rack3.getStoredWeight(), 7 * item1.getWeight());

        // Check total number of available units of each item
        Assert.assertEquals(item1.getAvailableUnits(), 5 + 1 + 7);
        Assert.assertEquals(item2.getAvailableUnits(), 3 + 6);
        Assert.assertEquals(item3.getAvailableUnits(), 7);
    }

    @Test
    public void reservationTest() {
        // General reservation of item units
        item1.reserve(6);

        Assert.assertEquals(item1.getAvailableUnits(), 7);
        Assert.assertEquals(item1.getReservedUnits(), 6);
        Assert.assertEquals(item1.getTotalUnits(), 5 + 1 + 7);

        // Specific reservation of item units in a rack
        rack2.reserve(item2, 4);

        Assert.assertEquals(rack2.get(item2), 2);
        Assert.assertEquals(item2.get(rack2), 2);

        Assert.assertEquals(item2.getReservedUnits(), 4);
        Assert.assertEquals(item2.getTotalUnits(), 3 + 6);
        Assert.assertEquals(item2.getAvailableUnits(), 5);
    }

    @Test
    public void orderSimulationTest() {
        // Simulate an order that require
        // 2 units of item1
        // 3 units of item2

        //
        // Initial items distribution
        //

        // item1 -> 13 units
        // item2 -> 9 units
        // item3 -> 7 units

        //
        // Initial racks distribution
        //

        // rack1 -> { item1: 5, item2: 2 }
        // rack2 -> { item1: 1, item2: 6, item3: 7 }
        // rack3 -> { item1: 7 }

        //
        // General reservation of needed order units must be done first
        //
        item1.reserve(2);
        item2.reserve(3);

        // Item 1
        Assert.assertEquals(item1.getReservedUnits(), 2);
        Assert.assertEquals(item1.getTotalUnits(), 13);
        Assert.assertEquals(item1.getAvailableUnits(), 13 - 2);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 3);
        Assert.assertEquals(item2.getTotalUnits(), 9);
        Assert.assertEquals(item2.getAvailableUnits(), 9 - 3);

        // Item 3
        Assert.assertEquals(item3.getReservedUnits(), 0);
        Assert.assertEquals(item3.getTotalUnits(), 7);
        Assert.assertEquals(item3.getAvailableUnits(), 7);

        //
        // Specific reservation of needed order units in specific racks
        //

        item1.reserve(-2);
        item2.reserve(-3);
        rack1.reserve(item1, 2);
        rack2.reserve(item2, 3);

        // Item 1
        Assert.assertEquals(item1.getReservedUnits(), 2);
        Assert.assertEquals(item1.getTotalUnits(), 13);
        Assert.assertEquals(item1.getAvailableUnits(), 13 - 2);
        Assert.assertEquals(item1.get(rack1), 5 - 2);
        Assert.assertEquals(rack1.get(item1), 5 - 2);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 3);
        Assert.assertEquals(item2.getTotalUnits(), 9);
        Assert.assertEquals(item2.getAvailableUnits(), 9 - 3);
        Assert.assertEquals(item2.get(rack2), 6 - 3);
        Assert.assertEquals(rack2.get(item2), 6 - 3);

        //
        // Acquiring reserved items in racks
        //
        rack1.reserve(item1, -2);
        rack2.reserve(item2, -3);
        rack1.add(item1, -2);
        rack2.add(item2, -3);

        // Item 1
        Assert.assertEquals(item1.getReservedUnits(), 0);
        Assert.assertEquals(item1.getTotalUnits(), 13 - 2);
        Assert.assertEquals(item1.getAvailableUnits(), 13 - 2);
        Assert.assertEquals(item1.get(rack1), 5 - 2);
        Assert.assertEquals(rack1.get(item1), 5 - 2);

        // Item 2
        Assert.assertEquals(item2.getReservedUnits(), 0);
        Assert.assertEquals(item2.getTotalUnits(), 9 - 3);
        Assert.assertEquals(item2.getAvailableUnits(), 9 - 3);
        Assert.assertEquals(item2.get(rack2), 6 - 3);
        Assert.assertEquals(rack2.get(item2), 6 - 3);
    }
}