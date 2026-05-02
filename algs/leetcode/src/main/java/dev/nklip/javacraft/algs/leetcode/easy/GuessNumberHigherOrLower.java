package dev.nklip.javacraft.algs.leetcode.easy;

/*
 * 374. Guess Number Higher or Lower
 *
 * LeetCode: https://leetcode.com/problems/guess-number-higher-or-lower/description/
 *
 * We are playing the Guess Game. The game is as follows:
 *
 * I pick a number from 1 to n. You have to guess which number I picked (the number I picked stays the same throughout the game).
 *
 * Every time you guess wrong, I will tell you whether the number I picked is higher or lower than your guess.
 *
 * You call a pre-defined API int guess(int num), which returns three possible results:
 *
 * 1) -1: Your guess is higher than the number I picked (i.e. num > pick).
 * 2) 1: Your guess is lower than the number I picked (i.e. num < pick).
 * 3) 0: your guess is equal to the number I picked (i.e. num == pick).
 *
 * Return the number that I picked.
 */
public class GuessNumberHigherOrLower {

    private final int pick;

    public GuessNumberHigherOrLower(int guess) {
        this.pick = guess;
    }

    public int guess(int guess) {
        return Integer.compare(this.pick, guess);
    }

    public int guessNumber(int n) {
        return guessNumber(1, n);
    }

    public int guessNumber(int min, int max) {
        int mid = max - (max - min) / 2;

        int comparison = guess(mid);
        if (comparison == 1) {
            return guessNumber(mid + 1, max);
        } else if (comparison == -1) {
            return guessNumber(min, mid - 1);
        } else {
            return mid;
        }
    }

}
