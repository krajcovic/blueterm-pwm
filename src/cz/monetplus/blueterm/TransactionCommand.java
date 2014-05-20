package cz.monetplus.blueterm;

public enum TransactionCommand {
    /**
     * 
     */
    UNKNOWN,

    /**
     * 
     */
    HANDSHAKE,

    /**
     * 
     */
    PAY,

    /**
     * Application master info(version).
     */
    INFO,

    /**
     * Connect only to terminal and wait for operation from terminal. Stahovani
     * klicu.
     */
    ONLYCONNECT,
}
