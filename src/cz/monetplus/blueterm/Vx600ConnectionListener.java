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
        messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                TerminalState.UNDEFINED,
                TransactionCommand.UNKNOWN.ordinal(), "VMF connected.");
    }

    @Override
    public void onConnectionFailed() {
        messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                TerminalState.UNDEFINED,
                TransactionCommand.UNKNOWN.ordinal(), "VMF connection failed");
        messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -6, 0, "VMF connection failed");

    }

    @Override
    public void onDisconnected(String arg0) {
        messageThread.addMessage(HandleMessages.MESSAGE_TOAST,
                TerminalState.UNDEFINED,
                TransactionCommand.UNKNOWN.ordinal(), "VMF disconected");
        messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -6, 0, "VMF disconected");

    }

    // @Override
    // public void onConnectionEstablished()
    // {
    // Runnable action = new Runnable()
    // {
    //
    // @Override
    // public void run()
    // {
    // mLoadingDialog.dismissAllowingStateLoss();
    // Toast.makeText(MainActivity.this, R.string.vx_connection_stablished,
    // Toast.LENGTH_SHORT).show();
    // changeControlsState(true);
    //
    // }
    //
    // };
    //
    // runOnUiThread(action);
    // }

    // @Override
    // public void onConnectionFailed()
    // {
    // Runnable action = new Runnable()
    // {
    // @Override
    // public void run()
    // {
    // changeControlsState(false);
    // mLoadingDialog.dismissAllowingStateLoss();
    //
    // Toast.makeText(MainActivity.this,
    // getString(R.string.vx_connection_failed_message),
    // Toast.LENGTH_SHORT).show();
    // }
    // };
    //
    // runOnUiThread(action);
    // }
    //
    // @Override
    // public void onDisconnected(final String deviceName)
    // {
    //
    // DeviceDisconnectedDialogFragment reconnectDialog = new
    // DeviceDisconnectedDialogFragment(
    // deviceName);
    // FragmentManager manager = getSupportFragmentManager();
    // FragmentTransaction transaction = manager.beginTransaction();
    // transaction.add(reconnectDialog, "reconnectDialog");
    // transaction.commitAllowingStateLoss();
    // changeControlsState(false);
    // }

}
