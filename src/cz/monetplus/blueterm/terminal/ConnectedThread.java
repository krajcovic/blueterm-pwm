package cz.monetplus.blueterm.terminal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.app.Activity;
import android.util.Log;

import com.verifone.vmf.api.VMF;
import com.verifone.vmf.api.VMF.AppLinkListener;
import com.verifone.vmf.api.VMF.PrinterDataListener;

import cz.monetplus.blueterm.HandleMessages;
import cz.monetplus.blueterm.MessageThread;
import cz.monetplus.blueterm.Vx600ConnectionListener;
import cz.monetplus.blueterm.util.MonetUtils;

/**
 * This thread runs during a connection with a remote device. It handles all
 * incoming and outgoing transmissions.
 */
public class ConnectedThread extends TerminalsThread {
    private static final int LISTEN_PORT = 33333;

    private static final int DESTINATION_ID = 128;

    private static final int APP_ID = 1;

    private static final String TAG = "ConnectedThread";

    private Activity activity;

    private ByPassTCPServerThread bypassServerThread;

    // private PipedOutputStream output;
    // private PipedInputStream input;

    public ConnectedThread(MessageThread messageThread, Activity activity)
            throws Exception {
        super(messageThread);

        Log.d(TAG, "create ConnectedThread: ");
        this.activity = activity;

        // try {
        // output = new PipedOutputStream();
        // input = new PipedInputStream(output);
        // } catch (IOException e) {
        // Log.e(TAG, e.getMessage());
        // }

        String help = VMF.vmfGetVersionLib();
        Log.i(TAG, "libVmf Version: " + help);

        help = VMF.vmfPrtGetVersionLib();
        Log.i(TAG, "libPrt Version: " + help);

        VMF.setAppLinkListener(new AppLinkReceiver());
        VMF.setPrinterDataListener(new DataReceiver());

        bypassServerThread = new ByPassTCPServerThread(messageThread,
                LISTEN_PORT);
        bypassServerThread.start();
    }

    private class AppLinkReceiver implements AppLinkListener {

        @Override
        public void onResponse(final byte[] recvBuf, final boolean timeOut) {
            Log.i(TAG, "Received hex: " + MonetUtils.bytesToHex(recvBuf)
                    + "Timeout: " + timeOut);
        }
    }

    private class DataReceiver implements PrinterDataListener {

        @Override
        public void onReceive(byte[] recvBuf) {
            Log.i(TAG, "Received hex: " + MonetUtils.bytesToHex(recvBuf));
        }

    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");

        if (VMF.vmfConnectVx600(activity, new Vx600ConnectionListener(), APP_ID) == 0) {

            VMF.vmfAppLinkSend(DESTINATION_ID,
                    ("127.0.0.1:" + LISTEN_PORT).getBytes(), 5000);

            // Keep listening to the InputStream while connected
            while (!Thread.currentThread().isInterrupted()
                    && bypassServerThread != null
                    && !bypassServerThread.isInterrupted()) {
                // todo: treba wait nebo tak neco.
                // todo: nebo kontrola, ze je vse v poradku
                // todo: nebo posilani ze jsem ready
            }
            
            VMF.vmfDisconnectVx600();

            // Ukonci to.
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, 0, 0, "OK");
        } else {
            messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -3, 0,
                    "VMF connection failed.");
        }
    }

    /**
     * Write to the connected OutStream.
     * 
     * @param buffer
     *            The bytes to write
     */
    @Override
    public void write(byte[] buffer) throws IOException {
        bypassServerThread.write(buffer);

    }

    @Override
    public void interrupt() {
        
        VMF.vmfDisconnectVx600();
        
        if (bypassServerThread != null) {
            bypassServerThread.interrupt();
            try {
                bypassServerThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bypassServerThread = null;
        }

        super.interrupt();
    }
}
