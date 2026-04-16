package dev.nklip.javacraft.algs.leetcode.easy;

import dev.nklip.javacraft.algs.leetcode.model.ListNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * 234. Palindrome Linked List
 *
 * LeetCode: https://leetcode.com/problems/palindrome-linked-list/
 *
 * Given the head of a singly linked list, return true if it is a palindrome or false otherwise.
 */
public class PalindromeLinkedList {

    // Time: O(n), Space: O(1)
    public boolean isPalindrome(ListNode head) {
        ListNode slow = head;
        ListNode fast = head;

        // 1. find middle list for slow
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        // 2. find the second half of the middle for slow
        ListNode prev = null;
        while (slow != null) {
            ListNode next = slow.next;
            slow.next = prev;
            prev = slow;
            slow = next;
        }

        // 3. Compare head and prev
        ListNode left = head;
        ListNode right = prev;
        while (right != null && left != null) {
            if (left.val != right.val) {
                return false;
            }
            right = right.next;
            left = left.next;
        }
        return true;
    }

    // Time: O(n), Space: O(n)
    public boolean isPalindromeUseTextToFindPalindrome(ListNode head) {
        List<Integer> charList = new ArrayList<>();

        while (head != null) {
            charList.add(head.val);
            head = head.next;
        }

        for (int i = 0, y = charList.size() - 1; i < charList.size(); i++, y--) {
            if (!Objects.equals(charList.get(i), charList.get(y))) {
                return false;
            }
            if (i > y) {
                return true;
            }
        }
        return true;
    }
}
