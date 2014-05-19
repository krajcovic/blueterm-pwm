package cz.monetplus.blueterm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import cz.monetplus.blueterm.bprotocol.BProtocol;
import cz.monetplus.blueterm.bprotocol.BProtocolFactory;
import cz.monetplus.blueterm.bprotocol.BProtocolMessages;
import cz.monetplus.blueterm.bprotocol.BProtocolTag;
import cz.monetplus.blueterm.frames.SLIPFrame;
import cz.monetplus.blueterm.frames.TerminalFrame;
import cz.monetplus.blueterm.server.ServerFrame;
import cz.monetplus.blueterm.terminal.TerminalCommands;
import cz.monetplus.blueterm.terminal.TerminalPorts;
import cz.monetplus.blueterm.terminal.TerminalServiceBT;
import cz.monetplus.blueterm.terminal.TerminalState;
import cz.monetplus.blueterm.util.MonetUtils;

//import android.R.bool;

/**
 * Thread for handling all messages.
 * 
 * @author "Dusan Krajcovic"
 * 
 */
public class MessageThread extends Thread {

    /**
     * String tag for logging.
     */
    private static final String TAG = "MessageThread";

    /**
     * Server connection ID. Only one serverconnection.
     */
    private byte[] serverConnectionID = null;

    /**
     * TCP client thread for read and write.
     */
    private static TCPClientThread tcpThread = null;

    /**
     * Message queue for handling messages from threads.
     */
    private final Queue<Message> queue = new LinkedList<Message>();

    /**
     * Application context.
     */
    private final Activity activity;

    /**
     * Terminal port (example 33333).
     */
    private final int terminalPort;

    /**
     * Transaction input params.
     */
    private final TransactionIn transactionInputData;

    /**
     * Transaction output params.
     */
    private TransactionOut transactionOutputData = new TransactionOut();

    /**
     * Stop this thread.
     */
    private boolean stopThread = false;

    /**
     * Member object for the chat services.
     */
    private TerminalServiceBT terminalService = null;

    /**
     * Terminal to muze posilat po castech.
     */
    private static ByteArrayOutputStream slipOutputpFraming = null;

    /**
     * 
     */
    private int currentTerminalState;

    /**
     * @param activity
     *            Current activity.
     * @param terminalPort
     *            Terminal socket port.
     * @param transactionInputData
     *            Transaction input data.
     */
    public MessageThread(final Activity activity, int terminalPort,
            TransactionIn transactionInputData) {
        super();

        slipOutputpFraming = new ByteArrayOutputStream();
        slipOutputpFraming.reset();

        this.activity = activity;
        this.terminalPort = terminalPort;
        this.transactionInputData = transactionInputData;
        this.transactionOutputData.setMessage("Upppsss upadla ti knihovna.");
    }

    @Override
    public void run() {
        while (!stopThread) {
            if (queue.peek() != null) {
                handleMessage(queue.poll());
            }
        }

        if (tcpThread != null) {
            tcpThread.interrupt();
            tcpThread = null;
        }
    }

    /**
     * Get result from current thread.
     * 
     * @return TransactionOut result Data.
     */
    public TransactionOut getValue() {
        return transactionOutputData;
    }

    /**
     * Create and send pay request to terminal.
     */
    private void pay() {
        this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1, SLIPFrame
                .createFrame(new TerminalFrame(terminalPort, BProtocolMessages
                        .getSale(transactionInputData.getAmount(),
                                transactionInputData.getCurrency(),
                                transactionInputData.getInvoice()))
                        .createFrame()));
    }

    /**
     * Create and send handshake to terminal.
     */
    private void handshake() {
        this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1, SLIPFrame
                .createFrame(new TerminalFrame(terminalPort, BProtocolMessages
                        .getHanshake()).createFrame()));
    }

    /**
     * Create and send app info request to terminal.
     */
    private void appInfo() {
        this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1, SLIPFrame
                .createFrame(new TerminalFrame(terminalPort, BProtocolMessages
                        .getAppInfo()).createFrame()));
    }

    /**
     * @param what
     *            HandleMessages.
     * @param terminalState
     *            TerminalState. Muze byt i datalength.
     * @param transactionCommand
     *            TransactionCommand
     * @param obj
     *            Data for executing messages.
     */

    public final void addMessage(int what, int terminalState,
            int transactionCommand, Object obj) {
        addMessage(Message.obtain(null, what, terminalState,
                transactionCommand, obj));
    }

    /**
     * @param what
     *            HandleMessages.
     * @param terminalState
     *            TerminalState.
     * @param transactionCommand
     *            TransactionCommand
     */
    public void addMessage(int what, int terminalState, int transactionCommand) {
        addMessage(Message
                .obtain(null, what, terminalState, transactionCommand));
    }

    /**
     * @param what
     *            HandleMessages.
     */
    public void addMessage(int what) {
        addMessage(Message.obtain(null, what));
    }

    /**
     * @param msg
     *            Message for addding to queue.
     */
    public void addMessage(Message msg) {
        queue.add(msg);
    }

    /**
     * @param service
     *            Terminal service serving bluetooth.
     */
    public void setTerminalService(TerminalServiceBT service) {
        this.terminalService = service;
    }

    public void handleMessage(final Message msg) {
        if (msg == null) {
            return;
        }
        switch (msg.what) {
        case HandleMessages.MESSAGE_STATE_CHANGE:
            handleStateChange(msg);
            break;

        case HandleMessages.MESSAGE_TERM_SEND_COMMAND:
            break;
        // case HandleMessages.MESSAGE_SERVER_WRITE:
        // break;
        // case HandleMessages.MESSAGE_SERVER_READ:
        // break;

        case HandleMessages.MESSAGE_TERM_WRITE:
            // Jedine misto v aplikaci pres ktere se posila do terminalu
            try {
                write2Terminal((byte[]) msg.obj);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                this.stopThread(e.hashCode(), e.getMessage());
            }
            break;

        case HandleMessages.MESSAGE_CONNECTED:
            // Send to terminal information about connection at server.
            connectionRequest(msg);
            break;

        case HandleMessages.MESSAGE_TERM_READ:
            handleTermReceived(msg);
            break;
        case HandleMessages.MESSAGE_DEVICE_NAME:
            // Nemam tuseni k cemu bych to vyuzil
            break;
        case HandleMessages.MESSAGE_TOAST:
            if (msg != null && msg.obj != null) {
                Log.i(TAG, msg.obj.toString());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, msg.obj.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            break;
        case HandleMessages.MESSAGE_QUIT:
            this.stopThread(msg.arg1, msg.obj.toString());
            break;
        }
    }

    /**
     * Sends a message.
     * 
     * @param message
     *            A string of text to send.
     * @throws IOException
     *             Input output exception by write to terminal.
     */
    private void write2Terminal(byte[] message) throws IOException {
        // Check that we're actually connected before trying anything
        if (terminalService.getState() != TerminalState.STATE_CONNECTED) {

            this.addMessage(HandleMessages.MESSAGE_TOAST, -1, -1,
                    R.string.not_connected);
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            terminalService.write(message);
        }
    }

    /**
     * Send to terminal information about connection at server.
     * 
     * @param msg
     *            Contains status(arg1) about current connection to server.
     * */
    private void connectionRequest(Message msg) {
        byte[] status = new byte[1];
        status[0] = (byte) msg.arg1;
        ServerFrame soFrame = new ServerFrame(
                TerminalCommands.TERM_CMD_SERVER_CONNECTED,
                serverConnectionID, status);
        TerminalFrame toFrame = new TerminalFrame(
                TerminalPorts.SERVER.getPortNumber(), soFrame.createFrame());

        this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1,
                SLIPFrame.createFrame(toFrame.createFrame()));
    }

    /**
     * Received message from terminal.
     * 
     * @param msg
     *            Messaget contains information read from terminal.
     */
    private void handleTermReceived(Message msg) {
        slipOutputpFraming.write((byte[]) msg.obj, 0, msg.arg1);

        // Check
        if (SLIPFrame.isFrame(slipOutputpFraming.toByteArray())) {

            TerminalFrame termFrame = new TerminalFrame(
                    SLIPFrame.parseFrame(slipOutputpFraming.toByteArray()));
            slipOutputpFraming.reset();

            if (termFrame != null) {
                switch (termFrame.getPort()) {
                case UNDEFINED:
                    Log.d(TAG, "undefined port");
                    break;
                case SERVER:
                    // messages for server
                    handleServerMessage(termFrame);
                    break;
                case FLEET:
                    Log.d(TAG, "fleet data");
                    break;
                case MAINTENANCE:
                    Log.d(TAG, "maintentace data");
                    break;
                case MASTER:
                    // Tyhle zpravy zpracovavat, jsou pro tuhle
                    // aplikaci
                    BProtocol bprotocol = new BProtocolFactory()
                            .deserialize(termFrame.getData());

                    if (bprotocol.getProtocolType().equals("B0")) {
                        String message = "Terminal working(B0)...";

                        this.addMessage(HandleMessages.MESSAGE_TOAST, -1, -1,
                                message);
                    }

                    if (bprotocol.getProtocolType().equals("B2")) {
                        executeB2(bprotocol);
                    }

                    break;
                default:
                    // Nedelej nic, spatne data, format, nebo
                    // crc
                    Log.e(TAG, "Invalid port");
                    break;

                }
            }

        } else {
            Log.e(TAG, "Corrupted data. It's not slip frame.");
        }
    }

    private void executeB2(BProtocol bprotocol) {
        transactionOutputData = new TransactionOut();
        try {
            transactionOutputData.setResultCode(Integer.valueOf(bprotocol
                    .getTagMap().get(BProtocolTag.ResponseCode)));
        } catch (Exception e) {
            transactionOutputData.setResultCode(-1);
        }
        transactionOutputData.setMessage(bprotocol.getTagMap().get(
                BProtocolTag.ServerMessage));
        try {
            transactionOutputData.setAuthCode(Integer.valueOf(bprotocol
                    .getTagMap().get(BProtocolTag.AuthCode)));
        } catch (Exception e) {
            transactionOutputData.setAuthCode(0);
        }
        try {
            transactionOutputData.setSeqId(Integer.valueOf(bprotocol
                    .getTagMap().get(BProtocolTag.SequenceId)));
        } catch (Exception e) {
            transactionOutputData.setSeqId(0);
        }
        transactionOutputData.setCardNumber(bprotocol.getTagMap().get(
                BProtocolTag.PAN));
        transactionOutputData.setCardType(bprotocol.getTagMap().get(
                BProtocolTag.CardType));

        this.stopThread(transactionOutputData.getResultCode(),
                transactionOutputData.getMessage());
    }

    private void stopThread(Integer resultCode, String resultMessage) {
        transactionOutputData.setResultCode(resultCode);
        transactionOutputData.setMessage(resultMessage);

        terminalService.stop();
        stopThread = true;
    }

    // private void stopThread() {
    // terminalService.stop();
    // stopThread = true;
    // }

    private void handleStateChange(Message msg) {
        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + getCurrentTerminalState()
                + " -> " + msg.arg1);
        this.currentTerminalState = msg.arg1;

        switch (msg.arg1) {
        case TerminalState.STATE_CONNECTED:
            if (msg.arg2 >= 0) {
                switch (TransactionCommand.values()[msg.arg2]) {
                case HANDSHAKE:
                    handshake();
                    break;
                case INFO:
                    appInfo();
                    break;
                case PAY:
                    pay();
                    break;
                case ONLYCONNECT:
                    break;
                case UNKNOWN:
                    break;
                default:
                    break;

                }
            }
            break;
        case TerminalState.STATE_CONNECTING:
        case TerminalState.STATE_LISTEN:
            break;
        case TerminalState.STATE_NONE:
            break;
        }
    }

    private void handleServerMessage(TerminalFrame termFrame) {
        // sends the message to the server
        final ServerFrame serverFrame = new ServerFrame(termFrame.getData());

        Log.d(TAG, "Server command: " + serverFrame.getCommand());
        switch (serverFrame.getCommand()) {

        case TERM_CMD_ECHO:
            echoResponse(termFrame, serverFrame);
            break;

        case TERM_CMD_CONNECT:
            this.addMessage(HandleMessages.MESSAGE_TOAST, -1, -1,
                    "Connecting to server...");
            serverConnectionID = serverFrame.getId();

            int port = MonetUtils.getInt(serverFrame.getData()[4],
                    serverFrame.getData()[5]);

            int timeout = MonetUtils.getInt(serverFrame.getData()[6],
                    serverFrame.getData()[7]);

            // connect to the server
            tcpThread = new TCPClientThread(this);
            tcpThread.setConnection(
                    Arrays.copyOfRange(serverFrame.getData(), 0, 4), port,
                    timeout, serverFrame.getIdInt());
            Log.i(TAG, "TCP thread starting.");
            tcpThread.start();

            TerminalFrame responseTerminal = new TerminalFrame(termFrame
                    .getPort().getPortNumber(), new ServerFrame(
                    TerminalCommands.TERM_CMD_CONNECT_RES,
                    serverFrame.getId(), new byte[1]).createFrame());

            this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1,
                    SLIPFrame.createFrame(responseTerminal.createFrame()));

            break;

        case TERM_CMD_DISCONNECT:
            if (tcpThread != null) {
                tcpThread.interrupt();
                tcpThread = null;
            }
            break;

        case TERM_CMD_SERVER_WRITE:
            // Send data to server.
            tcpThread.sendMessage(serverFrame.getData());
        default:
            break;
        }

    }

    /**
     * Terminal check this application.
     * 
     * @param termFrame
     *            Terminal frame.
     * @param serverFrame
     *            Server frame.
     */
    private void echoResponse(TerminalFrame termFrame,
            final ServerFrame serverFrame) {
        TerminalFrame responseTerminal = new TerminalFrame(termFrame.getPort()
                .getPortNumber(),
                new ServerFrame(TerminalCommands.TERM_CMD_ECHO_RES,
                        serverFrame.getId(), null).createFrame());

        this.addMessage(HandleMessages.MESSAGE_TERM_WRITE, -1, -1,
                SLIPFrame.createFrame(responseTerminal.createFrame()));
    }

    public synchronized int getCurrentTerminalState() {
        return currentTerminalState;
    }
}
