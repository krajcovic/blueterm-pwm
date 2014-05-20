package cz.monetplus.blueterm;

import com.verifone.vmf.api.VMF.ConnectionListener;

public class Vx600ConnectionListener implements ConnectionListener {
    MessageThread messageThread;

    public Vx600ConnectionListener(MessageThread messageThread) {

        this.messageThread = messageThread;
    }

    @Override
    public final void onConnectionEstablished() {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    "VMF connected.");
        }
    }

    @Override
    public final void onConnectionFailed() {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    MonetBTAPIError.VMF_CONNECTION_FAILED.getMessage());
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT,
                    MonetBTAPIError.VMF_CONNECTION_FAILED);
        }

    }

    @Override
    public final void onDisconnected(String arg0) {
        if (messageThread != null) {
            messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                    "VMF disconected");
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT,
                    MonetBTAPIError.VMF_DISCONNECTED);
        }

    }
}
