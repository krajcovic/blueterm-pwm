package cz.monetplus.blueterm;

public interface TransactionIn {

    public abstract Integer getAmount();

    public abstract void setAmount(Integer amount);

    public abstract String getInvoice();

    public abstract void setInvoice(String invoice);

    public abstract Integer getCurrency();

    public abstract void setCurrency(Integer currency);

    public abstract TransactionCommand getCommand();

    public abstract void setCommand(TransactionCommand command);

    public abstract String getHostIP();

    public abstract void setHostIP(String hostIP);

    public abstract int getHostPort();

    public abstract void setHostPort(int hostPort);

}
