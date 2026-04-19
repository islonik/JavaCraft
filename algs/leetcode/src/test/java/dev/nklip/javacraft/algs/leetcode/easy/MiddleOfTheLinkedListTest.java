package dev.nklip.javacraft.algs.leetcode.easy;

import dev.nklip.javacraft.algs.leetcode.model.ListNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MiddleOfTheLinkedListTest {

    @Test
    void testCase1() {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(5);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;

        MiddleOfTheLinkedList solution = new MiddleOfTheLinkedList();

        ListNode actualResult = solution.middleNode(node1);

        Assertions.assertEquals(3, actualResult.val);
        Assertions.assertEquals(4, actualResult.next.val);
        Assertions.assertEquals(5, actualResult.next.next.val);
    }

    @Test
    void testCase2() {
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        ListNode node5 = new ListNode(5);
        ListNode node6 = new ListNode(6);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;
        node5.next = node6;

        MiddleOfTheLinkedList solution = new MiddleOfTheLinkedList();

        ListNode actualResult = solution.middleNode(node1);

        Assertions.assertEquals(4, actualResult.val);
        Assertions.assertEquals(5, actualResult.next.val);
        Assertions.assertEquals(6, actualResult.next.next.val);
    }
}
