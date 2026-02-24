import org.junit.Test;

import java.util.Iterator;

/**
 * @author Lipatov Nikita
 */
public class UnionFindTest {

    @Test
    public void simpleTest() {
        UF uf = new UF(10);
        uf.union(4, 0);
        uf.union(6, 3);
        uf.union(8, 0);
        uf.union(6, 2);
        uf.union(4, 1);
        uf.union(9, 4);
        // 4 4 6 6 4 5 6 7 4 4
    }

    @Test
    public void sort() {

        // 82 73 94 44 78 93 99 43 55 22
        // 95 62 28
        RedBlackBST<String, Integer> rd = new RedBlackBST();
        rd.put("82", 82);
        rd.put("73", 73);
        rd.put("94", 94);
        rd.put("44", 44);
        rd.put("78", 78);
        rd.put("93", 93);
        rd.put("99", 99);
        rd.put("43", 43);
        rd.put("55", 55);
        rd.put("22", 22);

        rd.put("95", 95);
        rd.put("62", 62);
        rd.put("28", 28);
    }



    public String toString(BST bst) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator iter = bst.levelOrder().iterator();
        while(iter.hasNext()) {
            stringBuilder.append(iter.next() + " ");
        }
        return stringBuilder.toString();
    }
}
