package ua.regesha.mkr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumberListTest {
    @Test
    public void testBitwiseOr() {
        NumberListImpl<Integer> n1 = new NumberListImpl<>();
        n1.add(1); n1.add(0);
        NumberListImpl<Integer> n2 = new NumberListImpl<>();
        n2.add(1); n2.add(1);
        // 10 | 11 = 11
        NumberListImpl<Integer> res = NumberListImpl.bitwiseOr(n1, n2);
        Assertions.assertEquals("[1, 1]", res.toString());
    }
}