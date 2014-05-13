package cz.monetplus.blueterm.terminal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cz.monetplus.blueterm.MessageThread;

import android.util.Log;

public class ByPassTCPServerThread extends TerminalsThread {

    private static final String TAG = "ByPassTCPServerThread";

    private ServerSocket serverSocket = null;
    private ByPassReceiverThread commThread = null;
    private final Integer listenPort;

    public ByPassTCPServerThread(MessageThread messageThread, Integer listenPort) {
        super(messageThread);
        this.listenPort = listenPort;
    }

    @Override
    public void run() {
        Socket socket = null;
        Integer returnCode = 0;
        String returnMessage = "Undefined interrupt.";

        try {
            serverSocket = new ServerSocket(listenPort);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        try {
            serverSocket.setSoTimeout(10000);
            socket = serverSocket.accept();

            if (commThread != null) {
                commThread.interrupt();
            }

            commThread = new ByPassReceiverThread(messageThread, socket);
            commThread.start();

            setState(TerminalState.STATE_CONNECTED);

            while (!Thread.currentThread().isInterrupted()
                    && commThread != null && !commThread.isInterrupted()) {
                // TODO: treba kontrolovat ze je tcp server v poradku.
                try {
                    sleep(100);
                } catch (InterruptedException e) {

                }
            }
        } catch (IOException e) {
            if (e != null) {
                e.printStackTrace();
            }
            returnCode = -2;
            returnMessage = "Exception by ByPassTCPServer.";
        }

        connectionLost(returnCode, returnMessage);
    }

    public void write(byte[] buffer) throws IOException {
        if (commThread != null && commThread.isAlive()) {
            commThread.write(buffer);
        } else {
            Log.e(TAG, "Communication thread isn't running.");
        }

    }

    @Override
    public void interrupt() {
        if (this.commThread != null) {
           // do {
                try {
                    this.commThread.interrupt();
                    this.commThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //} while (this.commThread.isAlive());
            this.commThread = null;
        }

        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
                this.serverSocket = null;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.interrupt();

    }

}
