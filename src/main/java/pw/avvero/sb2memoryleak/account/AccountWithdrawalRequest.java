package pw.avvero.sb2memoryleak.account;

import lombok.Data;

@Data
public class AccountWithdrawalRequest {

    private Integer id;
    private Integer amount;

}
