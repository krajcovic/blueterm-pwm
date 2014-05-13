package cz.monetplus.blueterm.terminal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.app.Activity;
import android.util.Log;

import com.verifone.vmf.api.VMF;
import com.verifone.vmf.Constants;
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
        // VMF.setPrinterDataListener(new DataReceiver());

        bypassServerThread = new ByPassTCPServerThread(messageThread,
                LISTEN_PORT);
        bypassServerThread.start();
    }

    private class AppLinkReceiver implements AppLinkListener {

        @Override
        public void onResponse(final byte[] recvBuf, final boolean timeOut) {
            if (recvBuf != null) {
                Log.i(TAG, "Received hex: " + MonetUtils.bytesToHex(recvBuf)
                        + "Timeout: " + timeOut);
            }
            
            if(timeOut) {
                messageThread.addMessage(HandleMessages.MESSAGE_QUIT, -10, 0,
                        "AppLinkReceiver timeout!");
            }
        }
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");

        Log.e(TAG, "calling vmfConnectVx600");
        if (VMF.vmfConnectVx600(activity, new Vx600ConnectionListener(
                messageThread), APP_ID) == 0) {

            int vmfAppLinkSend = VMF.vmfAppLinkSend(DESTINATION_ID,
                    ("127.0.0.1:" + LISTEN_PORT).getBytes(), 5000 * 1000);
           
//            VMF.VMF_ERROR.VMF_OK;

            // Keep listening to the InputStream while connected
            while (!Thread.currentThread().isInterrupted()
                    && VMF.isVx600Connected() && bypassServerThread != null
                    && !bypassServerThread.isInterrupted()) {
                // todo: treba wait nebo tak neco.
                // todo: nebo kontrola, ze je vse v poradku
                // todo: nebo posilani ze jsem ready

                try {
                    sleep(100);
                } catch (InterruptedException e) {

                }
            }

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
        // VMF.vmfAppLinkSend(DESTINATION_ID, buffer, 5000 * 1000);

    }

    @Override
    public void interrupt() {

        VMF.setAppLinkListener(null);

        if (bypassServerThread != null) {
//            do {
                try {
                    bypassServerThread.interrupt();
                    bypassServerThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            } while (bypassServerThread.isAlive());
            bypassServerThread = null;
        }

        super.interrupt();
        
        // Zkus pockat az si VMF dokecas.
        try {
            sleep(2000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
//            e1.printStackTrace();
        }
        
        // VMF.vmfDisconnectVx600();       
        while (VMF.isVx600Connected()) {
            try {
                Log.e(TAG, "calling vmfDisconnectVx600");
                VMF.vmfDisconnectVx600();
                sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }

        }

    }
}
