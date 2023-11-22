package pw.avvero.sb2memoryleak.account;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(Integer id) {
        super(String.format("Account %s is not found", id));
    }
}
