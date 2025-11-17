public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ВАРІАНТ 20: Банківський рахунок (Thread + Runnable) ===\n");

        // --- ЧАСТИНА 1: ДЕМОНСТРАЦІЯ ПРОБЛЕМИ ---
        System.out.println("--- 1. Unsafe Demonstration (Race Condition) ---");
        BankAccount unsafeAccount = new UnsafeBankAccount(100);

        // Створюємо два потоки, що намагаються зняти 100 одночасно
        // Runnable обгортаємо в Thread
        Thread t1 = new Thread(new Withdrawer(unsafeAccount, 100));
        Thread t2 = new Thread(new Withdrawer(unsafeAccount, 100));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("Unsafe Final Balance: " + unsafeAccount.getBalance());
        if (unsafeAccount.getBalance() < 0) {
            System.out.println("ВИСНОВОК: Баланс від'ємний! Синхронізація відсутня.");
        }

        System.out.println("\n--------------------------------------------------\n");

        // --- ЧАСТИНА 2: БЕЗПЕЧНА РОБОТА (WAIT/NOTIFY) ---
        System.out.println("--- 2. Safe Demonstration (Wait/Notify) ---");
        BankAccount safeAccount = new SafeBankAccount(0); // Починаємо з 0

        // Створюємо потік, що хоче зняти 500 (Runnable)
        Thread withdrawer = new Thread(new Withdrawer(safeAccount, 500), "Withdrawer-Thread");

        // Створюємо потік, що буде поповнювати по 100 (Thread)
        Depositor depositor = new Depositor(safeAccount, "Depositor-Thread");

        withdrawer.start(); // Почне чекати, бо грошей 0

        Thread.sleep(1000); // Даємо час переконатися, що він чекає

        depositor.start(); // Починає поповнювати

        withdrawer.join();
        depositor.join();

        System.out.println("Safe Final Balance: " + safeAccount.getBalance());
    }
}