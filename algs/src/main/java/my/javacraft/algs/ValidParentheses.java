package my.javacraft.algs;

/**
 * Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.
 *
 * An input string is valid if:
 *
 * 1. Open brackets must be closed by the same type of brackets.
 * 2. Open brackets must be closed in the correct order.
 * 3. Every close bracket has a corresponding open bracket of the same type.
 */
public class ValidParentheses {

    public boolean isValid(String s) {
        char []strs = s.toCharArray();

        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            char currCh = strs[i];
            temp.append(currCh);
            if (temp.length() > 1) {
                char lastCh = temp.charAt(temp.length() - 2);

                if (lastCh == '(' && currCh == ')') {
                    temp.deleteCharAt(temp.length() - 1);
                    temp.deleteCharAt(temp.length() - 1);
                } else if (lastCh == '{' && currCh == '}') {
                    temp.deleteCharAt(temp.length() - 1);
                    temp.deleteCharAt(temp.length() - 1);
                } else if (lastCh == '[' && currCh == ']') {
                    temp.deleteCharAt(temp.length() - 1);
                    temp.deleteCharAt(temp.length() - 1);
                }

                if (lastCh == '(' && (currCh == '}' || currCh == ']')) {
                    return false;
                } else if (lastCh == '{' && (currCh == ')' || currCh == ']')) {
                    return false;
                } else if (lastCh == '[' && (currCh == '}' || currCh == ')')) {
                    return false;
                }
            }
        }

        return temp.toString().equals("");
    }

}
