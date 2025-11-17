import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeBankAccountTest {

    /**
     * Тест 1: Перевірка базової логіки (послідовне виконання).
     * Перевіряємо, що гроші коректно додаються і знімаються в одному потоці.
     */
    @Test
    void testBasicOperations() {
        SafeBankAccount account = new SafeBankAccount(0);
        account.deposit(100);
        assertEquals(100, account.getBalance(), "Баланс має бути 100 після депозиту");

        account.withdraw(50);
        assertEquals(50, account.getBalance(), "Баланс має бути 50 після зняття");
    }

    /**
     * Тест 2: Перевірка блокування (wait).
     * Потік на зняття має чекати, поки не з'являться гроші.
     * Ми запускаємо зняття, чекаємо трохи, перевіряємо, що баланс не змінився (бо потік чекає),
     * а потім додаємо гроші і перевіряємо успішне завершення.
     */
    @Test
    void testWithdrawWaitsForDeposit() throws InterruptedException {
        SafeBankAccount account = new SafeBankAccount(0);

        // Цей потік хоче зняти 100, але на рахунку 0. Він має "заснути".
        Thread withdrawThread = new Thread(() -> account.withdraw(100));
        withdrawThread.start();

        // Даємо потоку час запуститися і увійти в стан wait()
        Thread.sleep(100);

        // Перевіряємо: баланс все ще 0, бо операція не завершилася (вона чекає)
        assertEquals(0, account.getBalance(), "Баланс не мав змінитися, потік має чекати");

        // Тепер вносимо гроші з головного потоку
        account.deposit(100);

        // Чекаємо завершення потоку зняття
        withdrawThread.join();

        // Тепер баланс має бути 0 (було 0 -> стало 100 -> зняли 100 -> стало 0)
        assertEquals(0, account.getBalance(), "Баланс має бути 0 після успішного завершення транзакцій");
    }

    /**
     * Тест 3: Багатопотоковий стрес-тест.
     * Запускаємо багато потоків на зняття і поповнення одночасно.
     * У кінці баланс має зійтися (не має бути Race Condition).
     */
    @Test
    void testConcurrencyStressTest() throws InterruptedException {
        int initialBalance = 1000;
        SafeBankAccount account = new SafeBankAccount(initialBalance);

        int numberOfThreads = 100;
        Thread[] threads = new Thread[numberOfThreads];

        // Запускаємо 100 потоків: 50 поповнюють на 10, 50 знімають по 10.
        for (int i = 0; i < numberOfThreads; i++) {
            if (i % 2 == 0) {
                threads[i] = new Thread(() -> account.deposit(10));
            } else {
                threads[i] = new Thread(() -> account.withdraw(10));
            }
            threads[i].start();
        }

        // Чекаємо завершення всіх потоків
        for (Thread t : threads) {
            t.join();
        }

        // Математика: 1000 + (50 * 10) - (50 * 10) = 1000.
        // Якщо синхронізація працює правильно, баланс не зміниться.
        assertEquals(initialBalance, account.getBalance(), "Фінальний баланс має дорівнювати початковому");
    }

    /**
     * Тест 4: Перевірка, що баланс не йде в мінус.
     */
    @Test
    void testBalanceNeverNegative() throws InterruptedException {
        SafeBankAccount account = new SafeBankAccount(50);

        // Спробуємо зняти 100 (більше ніж є)
        Thread t = new Thread(() -> account.withdraw(100));
        t.start();

        Thread.sleep(100);

        // Баланс має залишитися 50 (операція заблокована)
        assertTrue(account.getBalance() >= 0, "Баланс не повинен бути від'ємним");
        assertEquals(50, account.getBalance());

        // Зупиняємо завислий потік, бо тест закінчився
        t.interrupt();
    }
}