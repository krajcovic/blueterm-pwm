package cz.monetplus.blueterm;

public class TransactionInBase implements TransactionIn {

    private String hostIP;
    private int hostPort;
    private TransactionCommand command;
    private Integer amount;
    private String invoice;
    private Integer currency;

    public TransactionInBase() {
        super();
    }

    @Override
    public final Integer getAmount() {
        return amount;
    }

    @Override
    public final void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Override
    public final String getInvoice() {
        return invoice;
    }

    @Override
    public final void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    @Override
    public final Integer getCurrency() {
        return currency;
    }

    @Override
    public final void setCurrency(Integer currency) {
        this.currency = currency;
    }

    @Override
    public final TransactionCommand getCommand() {
        return command;
    }

    @Override
    public final void setCommand(TransactionCommand command) {
        this.command = command;
    }

    @Override
    public final String getHostIP() {
        return hostIP;
    }

    @Override
    public final void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    @Override
    public final int getHostPort() {
        return hostPort;
    }

    @Override
    public final void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

}
