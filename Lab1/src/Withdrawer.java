public class Withdrawer implements Runnable {
    private final BankAccount account;
    private final int amountToWithdraw;

    public Withdrawer(BankAccount account, int amountToWithdraw) {
        this.account = account;
        this.amountToWithdraw = amountToWithdraw;
    }

    @Override
    public void run() {
        account.withdraw(amountToWithdraw);
    }
}