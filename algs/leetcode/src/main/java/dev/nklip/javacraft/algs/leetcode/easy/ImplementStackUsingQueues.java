package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.LinkedList;
import java.util.Queue;

/*
 * 225. Implement Stack using Queues
 *
 * LeetCode: https://leetcode.com/problems/implement-stack-using-queues/
 *
 * Implement a last-in-first-out (LIFO) stack using only two queues.
 * The implemented stack should support all the functions of a normal stack (push, top, pop, and empty).
 *
 * Implement the ImplementStackUsingQueues class:
 *
 * 1) void push(int x) Pushes element x to the top of the stack.
 * 2) int pop() Removes the element on the top of the stack and returns it.
 * 3) int top() Returns the element on the top of the stack.
 * 4) boolean empty() Returns true if the stack is empty, false otherwise.
 *
 * Notes:
 *
 * 1) You must use only standard operations of a queue, which means that only push to back,
 * peek/pop from front, size and is empty operations are valid.
 *
 * 2) Depending on your language, the queue may not be supported natively.
 * You may simulate a queue using a list or deque (double-ended queue) as long as you use only
 * a queue's standard operations.
 */
public class ImplementStackUsingQueues<T> {

    private Queue<T> primary = new LinkedList<>();
    private Queue<T> secondary = new LinkedList<>();

    // time complexity - O(n)
    public void push(T x) {
        // secondary is always empty when we add 1,2,3
        secondary.offer(x);

        // add 1: primary is empty for 1, nothing happens
        // add 2: primary is not empty, contains 1
        // so we add 1 to 2
        // result: secondary is 2,1
        // add 3: primary is not empty, contains 2,1
        // so we add 2,1 to 3
        // result: secondary 3,2,1
        while (!primary.isEmpty()) {
            secondary.offer(primary.poll());
        }

        // we clean secondary
        Queue<T> temp = primary;
        primary = secondary;
        secondary = temp;
    }

    // time complexity - O(1)
    public T pop() {
        return primary.poll();
    }

    // time complexity - O(1)
    public T top() {
        return primary.peek();
    }

    // time complexity - O(1)
    public boolean empty() {
        return primary.isEmpty();
    }

}
