# Hive
Hive is an automated smart warehousing system. It is a framework for designing warehouses, simulating and monitoring their performance, and deploying them in real life using robotic mobile agents.


<p align="center">
  <img width="40%" height="40%" src="https://raw.githubusercontent.com/OmarBazaraa/Hive_backend/master/img/logo_trans.png">
</p>

## Hive System Design
The system mainly consists of three components: 
* **Backend:** the server having the core logic.
* **Frontend:** the graphical user interface.
* **Hardware:** the actual hardware robots.


<p align="center">
  <img width="90%" height="90%" src="https://raw.githubusercontent.com/OmarBazaraa/Hive_backend/master/img/block_diagram.png">
</p>


### Hive Backend
The backend or the server is the core of the Hive system. It is responsible for the simulation and deployment of the warehouse automation system including handling refill and collect orders, order dispatching, multi-robot path planning, communication with the actual robots, communication with the frontend, and maintain their synchronization.

### Hive Frontend
The frontend is the graphical interface for the user that provides design and simulation capabilities through a user-friendly tool.


<p align="center">
  <img src="https://raw.githubusercontent.com/OmarBazaraa/Hive_backend/master/img/gui.png">
</p>


The code of the frontend can be found [here](https://github.com/i-radwan/Hive_frontend)

### Hive Hardware
The hardware are the actual robotic mobile agents that carry on tasks in the warehouse.

The design and the code of the hardware robots can be found [here](https://github.com/i-radwan/Hive_hardware)

## Used Libraries
1. Spark library for WebSocket services  
   `com.sparkjava:spark-core:2.8.0`
   
2. Org Json library for JSON objects parsing  
   `org.json:json:20180813`
   
3. Org jgrapht library  
    `org.jgrapht:jgrapht-core:1.3.0`
    
4. JUnit library for testing  
   `junit:junit:4.12`  
   `org.junit.jupiter:junit-jupiter:5.4.2`
   
## How to Use
1. Install Java 11 or higher.
2. Install the above libraries.
3. Install the Hive frontend from the above link.
4. Compile and run.
