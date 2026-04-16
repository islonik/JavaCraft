package dev.nklip.javacraft.algs.leetcode.easy;

import java.util.Stack;

/*
 * 232. Implement Queue using Stacks
 *
 * LeetCode: https://leetcode.com/problems/implement-queue-using-stacks/
 *
 * Implement a first in first out (FIFO) queue using only two stacks.
 * The implemented queue should support all the functions of a normal queue (push, peek, pop, and empty).
 *
 * Implement the ImplementQueueUsingStacks class:
 *
 * 1) void push(int x) Pushes element x to the back of the queue.
 * 2) int pop() Removes the element from the front of the queue and returns it.
 * 3) int peek() Returns the element at the front of the queue.
 * 4) boolean empty() Returns true if the queue is empty, false otherwise.
 *
 * Notes:
 *
 * You must use only standard operations of a stack,
 * which means only push to top, peek/pop from top, size, and is empty operations are valid.
 * Depending on your language, the stack may not be supported natively.
 * You may simulate a stack using a list or deque (double-ended queue) as long as you use only a stack's standard operations.
 */
public class ImplementQueueUsingStacks {

    private final Stack<Integer> inbox = new Stack<>();
    private final Stack<Integer> outbox = new Stack<>();

    public void push(int x) {
        inbox.push(x);
    }

    public int pop() {
        shiftIfCan();
        return outbox.pop();
    }

    public int peek() {
        shiftIfCan();
        return outbox.peek();
    }

    public boolean empty() {
        return inbox.isEmpty();
    }

    private void shiftIfCan() {
        // move a single element from inbox to outbox
        while (outbox.isEmpty() && !inbox.isEmpty()) {
            outbox.push(inbox.peek());
        }
    }
}
