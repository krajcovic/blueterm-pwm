package cz.monetplus.blueterm.terminal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

import cz.monetplus.blueterm.HandleMessages;
import cz.monetplus.blueterm.MessageThread;
import cz.monetplus.blueterm.SlipInputReader;
import cz.monetplus.blueterm.TransactionCommand;
import cz.monetplus.blueterm.util.MonetUtils;

public class ByPassReceiverThread extends TerminalsThread {

    private static final String TAG = "ByPassReceiverThread";

    private final Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;

    public ByPassReceiverThread(MessageThread messageThread, Socket clientSocket) throws Exception {
        super(messageThread);

        this.clientSocket = clientSocket;
        try {
            SlipInputReader.setExit(false);
            input = new DataInputStream(this.clientSocket.getInputStream());
            output = new DataOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed binding a sockets.");
        }
    }

    public void run() {
        int returnCode = 0;
        String returnMessage = "undefined interrupted.";

        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] buffer = SlipInputReader.read(input);
                if (buffer != null) {
                    Log.i(TAG,
                            "TCP read (hex): " + MonetUtils.bytesToHex(buffer));

                    // Send the obtained bytes to the UI Activity
                    if (messageThread != null) {
                        messageThread.addMessage(
                                HandleMessages.MESSAGE_TERM_READ,
                                buffer.length, 0, buffer);
                    }
                }
            } catch (SocketException e) {
                // OK.
                // e.printStackTrace();
                // returnMessage = "Socket closed.";
            } catch (IOException e) {
                e.printStackTrace();
                returnCode = -1;
                returnMessage = "ByPassReceiver failed";
                connectionLost(returnCode, returnMessage);
                break;
            }
        }
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        Log.i(TAG, "TCP write (hex): " + MonetUtils.bytesToHex(buffer));
        output.write(buffer);
        output.flush();
    }

    @Override
    public void interrupt() {

        Log.i(TAG, "ByPassReceiver interrupt");
        
        SlipInputReader.setExit(true);

        try {
            if (output != null) {
                output.flush();
                output.close();
                output = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (input != null) {
                input.close();
                input = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
                // clientSocket = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.interrupt();
    }

}
