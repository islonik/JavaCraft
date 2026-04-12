# 1. Breadth-First Search
<sub>[Back to solutions](../README.md#solutions)</sub>

Breadth First Search (BFS) is a graph traversal algorithm that starts from a source node and explores the graph level by level. First, it visits all nodes directly adjacent to the source. Then, it moves on to visit the adjacent nodes of those nodes, and this process continues until all reachable nodes are visited.

* BFS is different from DFS in a way that closest vertices are visited before others. We mainly traverse vertices level by level.
* Popular graph algorithms like Dijkstra's shortest path, Kahn's Algorithm, and Prim's algorithm are based on BFS.
* BFS itself can be used to detect cycle in a directed and undirected graph, find shortest path in an unweighted graph and many more problems.

## 1.1. Idea
The breadth-first search algorithm works by exploring all neighboring nodes before moving to nodes at the next level of depth. It starts from a chosen source node and visits all nodes directly connected to it, then proceeds level by level until all reachable nodes have been explored. This ensures that nodes are visited in order of their distance from the source.

Let’s go through a step-by-step breakdown of how breadth-first search operates:

1) Start from the source node: Choose a starting node to begin traversal.
2) Mark the source as visited: Keep a record of visited nodes to avoid repetition or loops.
3) Enqueue the source node: Use a queue (FIFO structure) to store nodes to be explored.
4) Process the queue: BFS dequeues a node, visits its unvisited neighbors, marks them visited, and enqueues them.
5) Move level by level: Continue until the queue is empty, visiting all nodes layer by layer.

This systematic, level-based approach ensures that BFS finds the shortest route from the source to any other node in an unweighted graph.

For example:

Tree:

        3
       / \
      9   20
         /  \
        15   7

Code:
```java
public List<List<Integer>> bfs(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
        int size = queue.size();
        List<Integer> level = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);

            if (node.left != null)  queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }

        result.add(level);
    }

    return result;
}
```

Output: [[3], [9, 20], [15, 7]]<br/>
This is the classic level-order traversal (LeetCode 102).

## 1.2. Illustration

![Intervals](images/medium/1_bfs-traversal.gif)

## 1.3. Complexity

Time: O(V + E)<br/>
Space: O(V)<br/>
Where V - vertices and E - edges<br/>

## 1.4. How to detect it should be used
Key signals that BFS is the right approach:

1) <b>Shortest path</b> in an unweighted graph/grid — BFS guarantees the shortest path when all edges have equal weight.
2) <b>Minimum number of steps/moves/operations</b> — any problem asking for the fewest transformations (e.g., word ladder, minimum knight moves).
3) <b>Level-order traversal</b> — explicitly processing nodes level by level in a tree or graph.
4) <b>Nearest</b> / <b>closest</b> — finding the closest node satisfying some condition (nearest gate, nearest zero in a matrix).
5) <b>Spread/expand simultaneously</b> — problems modeled as simultaneous expansion: rotting oranges, walls and gates, multi-source flood fill.
6) <b>All nodes at distance K</b> — anything requiring exploration by distance layers.

## 1.5. LeetCode problems

Easy
* https://leetcode.com/problems/flood-fill/

Medium
* https://leetcode.com/problems/binary-tree-level-order-traversal/
* https://leetcode.com/problems/word-ladder/
* https://leetcode.com/problems/number-of-islands/
* https://leetcode.com/problems/course-schedule/
* https://leetcode.com/problems/course-schedule-ii/
* https://leetcode.com/problems/minimum-height-trees/
* https://leetcode.com/problems/01-matrix/
* https://leetcode.com/problems/open-the-lock/
* https://leetcode.com/problems/rotting-oranges/
* https://leetcode.com/problems/shortest-path-in-binary-matrix/
* https://leetcode.com/problems/as-far-from-land-as-possible/

Hard
* https://leetcode.com/problems/word-ladder-ii/
* https://leetcode.com/problems/shortest-path-to-get-all-keys/


# 4. Intervals
<sub>[Back to solutions](../README.md#solutions)</sub>

The Interval pattern is a powerful way to reason about problems involving ranges of values, whether time spans, numeric ranges, or geometric spans. Each interval is defined by a start and an end; for example, [10,20] represents everything from 10 through 20.

> [!NOTE]
> Two intervals overlap if the start of one is less than or equal to the end of the other.

## 4.1. Idea
Interval questions are a subset of array questions where you are given an array of two-element arrays (an interval) and the two values represent a start and an end value. Interval questions are considered part of the array family but they involve some common techniques hence they are extracted out to this special section of their own.

An example interval array: [[1, 2], [4, 7]].

Interval questions can be tricky to those who have not tried them before because of the sheer number of cases to consider when they overlap.

Corner cases:
* No intervals
* Single interval
* Two intervals
* Non-overlapping intervals
* An interval totally consumed within another interval
* Duplicate intervals (exactly the same start and end)
* Intervals which start right where another interval ends - [[1, 2], [2, 3]]

## 4.2. Illustration

![Intervals](images/medium/4_intervals.webp)

## 4.3. Complexity

Time: O(N * log N) where N is the total number of intervals. In the beginning, since we sort the intervals, our algorithm will take O(N * log N) to run.<br/>
Space: O(N), as we need to return a list containing all the merged intervals.

## 4.4. How to detect it should be used

This approach is quite useful when dealing with intervals, overlapping items or merging intervals.

---

# 6. Two Heaps
<sub>[Back to solutions](../README.md#solutions)</sub>

Imagine you’re managing a busy airport. Flights are constantly landing and taking off, and you need to quickly find the next most important flight—an emergency landing or a VIP departure. At the same time, new flights must be integrated into the schedule. How do you track all this while finding the highest-priority flight quickly? Without an efficient data structure, you’d have to scan the entire schedule every time a decision is needed, which can be slow and error-prone as the number of flights grows. The time complexity of this inefficient system will be O(n) for each decision, where n is the number of flights because it requires scanning the entire schedule to find the highest-priority flight.

The solution is heaps.

<b>Heap</b> is a special data structure that helps you efficiently manage priorities. With a min heap, you can always find the flight with the earliest priority, and with a max heap, you can focus on flights that have been waiting for the longest—all while making updates quickly when new flights are added.

A heap is a specialized binary tree that satisfies the heap property:

* <b>Min heap</b>: The value of each node is smaller than or equal to the values of its children. The root node holds the minimum value. A min heap always prioritizes the minimum value.

* <b>Max heap</b>: The value of each node is greater than or equal to the values of its children. The root node holds the maximum value. A max heap always prioritizes the maximum value.

* <b>Priority queue</b>: A priority queue is an abstract data type retrieves elements based on their custom priority. It is often implemented using a heap for efficiency.

A heap is a specific data structure with a fixed ordering (min or max), while a priority queue is an abstract data type that handles custom priority requirements for elements.

> [!NOTE]
> A heap is a specific data structure with a fixed ordering (min or max), while a priority queue is an abstract data type that handles custom priority requirements for elements.

## 6.1. Idea

In many problems, where we are given a set of elements such that we can divide them into two parts. To solve the problem, we are interested in knowing the smallest element in one part and the biggest element in the other part. This pattern is an efficient approach to solve such problems.

This pattern uses two Heaps to solve these problems; A <b>Min Heap</b> to find the smallest element and a <b>Max Heap</b> to find the biggest element.

Let’s assume that x is the median of the list. This means that, half of the items in the list are smaller than (or equal to) x and other half is greater than (or equal to) x.

1. We can store the smaller part of the list in a <b>Max Heap</b>. We are using <b>Max Heap</b> because we are only interested in knowing the largest number in the first half of the list.
2. We can store the larger part of the list in a <b>Min Heap</b>. We are using <b>Min Heap</b> because we are only interested in knowing the smallest number in the second half of the list.
3. Inserting a number in a heap will take O(log N) (better than the brute force approach)
4. The median of the current list of numbers can be calculated from the top element of the two heaps.

## 6.2. Illustration

![Two heaps](images/medium/6_two_heaps.webp)

## 6.3. Complexity

Time: O (log N) for insertions, O(1) - to find median<br/>
Space: O(N)

## 6.4. How to detect it should be used

This approach is quite useful when dealing with the problems where we are given a set of elements such that we can divide them into two parts.

To be able to solve these kinds of problems, we want to know the smallest element in one part and the biggest element in the other part. Two Heaps pattern uses two Heap data structure to solve these problems; a <b>Min Heap</b> to find the smallest element and a <b>Max Heap</b> to find the biggest element.

LeetCode problems:
* LeetCode 480 - Sliding Window Median [hard]
* LeetCode 502 - IPO [hard]


---
