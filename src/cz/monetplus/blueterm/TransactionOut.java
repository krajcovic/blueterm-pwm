package cz.monetplus.blueterm;

public interface TransactionOut {

    public abstract void setCardType(String cardType);

    public abstract void setCardNumber(String cardNumber);

    public abstract String getCardNumber();

    public abstract void setSeqId(Integer seqId);

    public abstract Integer getSeqId();

    public abstract void setAuthCode(Integer authCode);

    public abstract Integer getAuthCode();

    public abstract void setMessage(String message);

    public abstract String getMessage();

    public abstract void setResultCode(Integer resultCode);

    public abstract Integer getResultCode();

    public abstract String getCardType();

}
