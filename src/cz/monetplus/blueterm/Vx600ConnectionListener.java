package cz.monetplus.blueterm;

import com.verifone.vmf.api.VMF.ConnectionListener;

import cz.monetplus.blueterm.terminal.TerminalState;

public class Vx600ConnectionListener implements ConnectionListener {
    MessageThread messageThread;

    public Vx600ConnectionListener(MessageThread messageThread) {

        this.messageThread = messageThread;
    }

    @Override
    public void onConnectionEstablished() {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    TerminalState.UNDEFINED,
                    TransactionCommand.UNKNOWN.ordinal(), "VMF connected.");
        }
    }

    @Override
    public void onConnectionFailed() {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    TerminalState.UNDEFINED,
                    TransactionCommand.UNKNOWN.ordinal(),
                    "VMF connection failed");
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -6, 0,
                    "VMF connection failed");
        }

    }

    @Override
    public void onDisconnected(String arg0) {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    TerminalState.UNDEFINED,
                    TransactionCommand.UNKNOWN.ordinal(), "VMF disconected");
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -6, 0,
                    "VMF disconected");
        }

    }
}
