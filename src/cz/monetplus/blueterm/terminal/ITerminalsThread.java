package cz.monetplus.blueterm.terminal;

import java.io.IOException;

public interface ITerminalsThread {
    void setState(int newState);
    
    void connectionLost(Integer resonCode, String reasonMessage);
    
    void write(byte[] buffer) throws IOException;
 
    void interrupt();
}
