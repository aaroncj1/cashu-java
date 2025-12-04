package cashu.java.wallet;

public interface WalletService {

    void receive(String proof);

    void send(String amount);

    void deposit(String amount);

    void withdraw(String amount);

    void checkBalance();
}
