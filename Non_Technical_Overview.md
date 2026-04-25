# Packet Tracer Simulator: A Simple Guide

Welcome to the Packet Tracer Simulator! 

If you are not a programmer or networking expert, don't worry. This guide will explain exactly what this software does using a very simple analogy.

## 🏙️ The City Analogy

Imagine a large city. 
* The **Routers** (the grey circles with numbers) are **Intersections**.
* The **Cables** (the grey lines connecting them) are **Roads**.
* The **Packets** (the moving colored dots) are **Delivery Trucks**.

The goal of this software is to figure out the absolute fastest route for a Delivery Truck to get from a starting intersection to its final destination.

### But there is a catch...
Not all roads are equal! Some roads are highways, and some are dirt paths. In our software, every cable has a "latency" (a delay or traffic jam). When you see a packet moving slowly across a line, it's because that specific "road" has a high latency weight.

---

## 🏎️ The Race: How do the algorithms work?

When you click the **🏆 Race All** button, the software uses 4 different "Brains" (Algorithms) to try and solve the maze. They all race at the exact same time!

1. 🔵 **Dijkstra (Blue Truck)**: The cautious driver. This driver checks every single road branching outwards in a circle until they eventually bump into the destination. They will absolutely find the best route, but they waste a lot of time checking roads going in the wrong direction.
2. 🟢 **A* (Green Truck)**: The driver with a GPS compass. This driver acts just like Dijkstra, but they know what direction the destination is in. If the destination is East, they will prioritize checking Eastward roads first. This is incredibly efficient!
3. 🟣 **Bellman-Ford (Purple Truck)**: The paranoid driver. This driver doesn't trust anything. They check *every single road in the entire city* multiple times just to be absolutely certain they found the best path. It works, but it takes massive amounts of brain power.
4. 🟠 **BFS (Orange Truck)**: The driver who hates turning. This driver doesn't care about traffic jams or dirt paths. They only care about taking the path with the fewest number of roads (hops). They might arrive first, but they often take a worse path in real life!

---

## 🕹️ Interactive Features (Try these out!)

### 1. Breaking the Network (Clicking Lines)
While the program is running, try using your mouse to **click on any grey line**. It will instantly turn **RED**. You just caused a massive car crash / cable break! 
Watch how the drivers instantly reroute their trucks to avoid the broken road.

### 2. 🔥 Harsh Benchmark Mode
If you want to see pure chaos, click this red button. The software will instantly trigger 50 rapid-fire disasters. For 5 seconds, roads will break randomly all over the city, and the start/end destinations will jump around. 
At the end of the 5 seconds, a graph will pop up showing you exactly which "Brain" survived the chaos with the least amount of effort. (Spoiler: Green A* almost always wins!).

### 3. 🌲 Show MST (Minimum Spanning Tree)
If you click this, a golden web will appear. Imagine you are the Mayor of the city, and you are completely out of money. You need to pave *just enough* roads so that every intersection is connected, but you want to spend the absolute minimum amount of money. The golden web is that exact layout!

### 4. 👣 Step-by-Step Mode
Ever wanted to literally see the "Brain" thinking? Click one of the Step-by-Step buttons. A new window opens. Click "Next Step" to watch the algorithm color the intersections one-by-one as it tries to hunt down the destination. It's like watching a maze-solving robot in slow motion!

---
*Enjoy testing the networks!*
