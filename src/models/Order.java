package models;

import utils.Constants.*;

import java.util.List;

public class Order {

    int id;
    int itemId;
    int itemCount;
    int deliveryGateId;

    OrderType type;

    List<Task> subTasks;
}
