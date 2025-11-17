public class UnsafeBankAccount implements BankAccount {
    private int balance;

    public UnsafeBankAccount(int initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public void deposit(int amount) {
        balance += amount;
        System.out.println("UNSAFE DEPOSIT: +" + amount + " | Balance: " + balance);
    }

    @Override
    public void withdraw(int amount) {
        if (balance >= amount) {
            // Імітуємо затримку, щоб гарантовано викликати Race Condition
            try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }

            balance -= amount;
            System.out.println("UNSAFE WITHDRAW: -" + amount + " | Balance: " + balance);

            if (balance < 0) {
                System.err.println("!!! ALARM: BALANCE IS NEGATIVE: " + balance);
            }
        } else {
            System.out.println("UNSAFE WITHDRAW: Not enough money for -" + amount);
        }
    }

    @Override
    public int getBalance() {
        return balance;
    }
}