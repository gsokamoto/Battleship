import java.net.*;
import java.io.*;

public class Server
{
    //initialize socket and input stream
    private Socket BattleShipSocket = null;
    private ServerSocket BattleShipServer;

    // constructor creates server socket
    public Server(int portNumber) throws IOException {
        BattleShipServer = new ServerSocket(portNumber);
    }

    /*
     * Starts accepting sockets and generates multiple games at once
     * Will NOT accept more connections until current game begins
     */
    public void runServer() throws IOException
    {
        while(true)
        {
            BattleShipSocket = BattleShipServer.accept();
            GameThread Game = new GameThread(BattleShipSocket);
            Game.start();
            BattleShipSocket = BattleShipServer.accept();
            Game.addPlayer2(BattleShipSocket);
        }
    }

    // main to run server
    public static void main(String args[]) throws IOException
    {
        Server server = new Server(5000);
        server.runServer();
    }
}
