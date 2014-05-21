package cz.monetplus.blueterm;

import org.apache.http.util.ByteArrayBuffer;

import cz.monetplus.blueterm.terminal.TerminalState;

public class MonetMessage {

    private final HandleMessages message;

    private final ByteArrayBuffer data;

    private final String toastMessage;

    private final MonetBTAPIError errorInfo;

    private final TerminalState terminalState;

    private final TransactionCommand transactionCommand;

    private final byte serverStatus;

    public MonetMessage(HandleMessages message) {

        this.message = message;
        this.data = null;
        this.toastMessage = null;
        this.errorInfo = null;
        this.terminalState = null;
        this.transactionCommand = null;
        this.serverStatus = 0;
    }

    public MonetMessage(HandleMessages message, byte[] buffer, int length) {
        this.message = message;
        data = new ByteArrayBuffer(length);
        getData().append(buffer, 0, length);
        toastMessage = null;
        this.errorInfo = null;
        this.terminalState = null;
        this.transactionCommand = null;
        this.serverStatus = 0;
    }

    public MonetMessage(HandleMessages message, String string) {

        this.message = message;
        this.toastMessage = string;
        data = null;
        this.errorInfo = null;
        this.terminalState = null;
        this.transactionCommand = null;
        this.serverStatus = 0;
    }

    public MonetMessage(HandleMessages message, MonetBTAPIError error) {
        this.message = message;
        this.errorInfo = error;
        data = null;
        toastMessage = null;
        this.terminalState = null;
        this.transactionCommand = null;
        this.serverStatus = 0;
    }

    public MonetMessage(HandleMessages message, TerminalState terminalState,
            TransactionCommand command) {
        this.message = message;
        this.errorInfo = null;
        data = null;
        toastMessage = null;
        this.terminalState = terminalState;
        this.transactionCommand = command;
        this.serverStatus = 0;
    }

    public MonetMessage(HandleMessages message, byte serverStatus) {
        this.message = message;
        this.errorInfo = null;
        data = null;
        toastMessage = null;
        this.terminalState = null;
        this.transactionCommand = null;
        this.serverStatus = serverStatus;
    }

    public final HandleMessages getMessage() {
        return message;
    }

    public final String getToastMessage() {
        return toastMessage;
    }

    public final MonetBTAPIError getErrorInfo() {
        return errorInfo;
    }

    public final TerminalState getTerminalState() {
        return terminalState;
    }

    public final TransactionCommand getTransactionCommand() {
        return transactionCommand == null ? TransactionCommand.UNKNOWN
                : transactionCommand;
    }

    public final ByteArrayBuffer getData() {
        return data;
    }

    public byte getServerStatus() {
        return serverStatus;
    }
}
