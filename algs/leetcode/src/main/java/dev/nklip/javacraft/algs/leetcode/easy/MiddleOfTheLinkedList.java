package dev.nklip.javacraft.algs.leetcode.easy;

import dev.nklip.javacraft.algs.leetcode.model.ListNode;

/*
 * 876. Middle of the Linked List
 *
 * LeetCode: https://leetcode.com/problems/middle-of-the-linked-list/
 *
 * Given the head of a singly linked list, return the middle node of the linked list.
 *
 * If there are two middle nodes, return the second middle node.
 */
public class MiddleOfTheLinkedList {

    public ListNode middleNode(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode slowNode = head;
        ListNode fastNode = head;

        while (true) {
            if (fastNode.next == null) {
                return slowNode;
            } else if (fastNode.next.next == null) {
                return slowNode.next;
            }
            slowNode = slowNode.next;
            fastNode = fastNode.next.next;
        }
    }

}
