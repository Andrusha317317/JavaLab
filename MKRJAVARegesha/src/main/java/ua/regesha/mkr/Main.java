package ua.regesha.mkr;

public class Main {
    public static void main(String[] args) {
        System.out.println("Start MKR Variant 20");

        NumberListImpl<Integer> num1 = new NumberListImpl<>();
        num1.add(1); num1.add(0); num1.add(1); // 5

        NumberListImpl<Integer> num2 = new NumberListImpl<>();
        num2.add(0); num2.add(1); num2.add(1); // 3

        System.out.println("Num1: " + num1);
        System.out.println("Num2: " + num2);

        NumberListImpl<Integer> res = NumberListImpl.bitwiseOr(num1, num2);
        System.out.println("OR Result (should be 1, 1, 1): " + res);

        res.changeScale();
        System.out.println("Ternary Result (should be 2, 1): " + res);
    }
}