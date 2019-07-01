# Hive
Hive is an automated smart warehousing system.  
Hive is a framework for designing warehouses, simulating and monitoring their performance, and deploying them in real life using robotic mobile agents.  

![alt text](https://raw.githubusercontent.com/OmarBazaraa/Hive_backend/master/img/logo.png)

The system mainly consists of three components: 
* **Backend:** the server having the core logic.
* **Frontend:** the graphical user-friendly design and simulation tool.
* **Hardware:** the actual robotic mobile agents.

![alt text](https://raw.githubusercontent.com/OmarBazaraa/Hive_backend/master/img/block_diagram.png)

## Hive backend
The backend or the server is the core of the Hive system. It is responsible for the simulation and deployment of the warehouse automation system including handling refill and collect orders, order dispatching, multi-robot path planning, communication with the actual robots and communication with the frontend.

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
   