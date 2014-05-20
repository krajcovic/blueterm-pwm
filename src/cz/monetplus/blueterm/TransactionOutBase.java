package cz.monetplus.blueterm;

/**
 * Base class for transaction results.
 * 
 * @author "Dusan Krajcovic dusan.krajcovic [at] monetplus.cz"
 * 
 */
public class TransactionOutBase implements TransactionOut {

    private Integer resultCode;
    private String message;
    private Integer authCode;
    private Integer seqId;
    private String cardNumber;
    protected String cardType;

    public TransactionOutBase() {
        super();
    }

    @Override
    public final Integer getResultCode() {
        return resultCode;
    }

    @Override
    public final void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public final String getMessage() {
        return message;
    }

    @Override
    public final void setMessage(String message) {
        this.message = message;
    }

    @Override
    public final Integer getAuthCode() {
        return authCode;
    }

    @Override
    public final void setAuthCode(Integer authCode) {
        this.authCode = authCode;
    }

    @Override
    public final Integer getSeqId() {
        return seqId;
    }

    @Override
    public final void setSeqId(Integer seqId) {
        this.seqId = seqId;
    }

    @Override
    public final String getCardNumber() {
        return cardNumber;
    }

    @Override
    public final void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public final void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public final String getCardType() {
        return cardType;
    }

}
