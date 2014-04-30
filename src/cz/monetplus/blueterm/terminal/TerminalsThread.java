package cz.monetplus.blueterm.terminal;

import cz.monetplus.blueterm.HandleMessages;
import cz.monetplus.blueterm.MessageThread;
import cz.monetplus.blueterm.TransactionCommand;

abstract public class TerminalsThread extends Thread implements
        ITerminalsThread {

    private static final String TAG = "TerminalsThread";

    protected final MessageThread messageThread;

    protected TerminalsThread(MessageThread messageThread) {
        super();
        this.messageThread = messageThread;
    }

    @Override
    public void connectionLost(Integer reasonCode, String reasonMessage) {
        if (messageThread != null) {
            // Send a failure message back to the Activity
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    TerminalState.UNDEFINED,
                    TransactionCommand.UNKNOWN.ordinal(), reasonMessage);
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, reasonCode, 0, reasonMessage);
        }
    }

    /**
     * Set the current state of the chat connection.
     * 
     * @param state
     *            An integer defining the current connection state.
     */
    @Override
    public synchronized void setState(int newState) {

        // Give the new state to the Handler so the UI Activity can update
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_STATE_CHANGE,
                    newState, -1);
        }
    } 
    
    @Override
    public void interrupt() {
        super.interrupt();
    }
}
