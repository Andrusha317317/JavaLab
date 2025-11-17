public class SafeBankAccount implements BankAccount {
    private int balance;

    public SafeBankAccount(int initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public synchronized void deposit(int amount) {
        balance += amount;
        System.out.println(Thread.currentThread().getName() + " deposited: " + amount + " | Balance: " + balance);
        // Повідомляємо потоки, які чекають (withdraw), що баланс змінився
        notifyAll();
    }

    @Override
    public synchronized void withdraw(int amount) {
        System.out.println(Thread.currentThread().getName() + " wants to withdraw: " + amount);

        // Поки грошей недостатньо - чекаємо (звільняємо монітор)
        while (balance < amount) {
            try {
                System.out.println(Thread.currentThread().getName() + " waiting for funds...");
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted during wait");
                return;
            }
        }

        // Коли прокинулись і грошей вистачає:
        balance -= amount;
        System.out.println(Thread.currentThread().getName() + " withdrew: " + amount + " | Balance: " + balance);
    }

    @Override
    public int getBalance() {
        return balance;
    }
}