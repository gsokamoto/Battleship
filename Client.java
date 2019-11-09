import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client
{
    private Socket GameSocket		 = null;
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    private Message msg = null;
    private Scanner inputScanner;
    private String playerInput;

    // constructor
    public Client() throws IOException
    {
        GameSocket = new Socket("localhost", 5000);
        inputScanner = new Scanner(System.in);
        output = new ObjectOutputStream(GameSocket.getOutputStream());
    }

    /*
     * Checks message type and starts function accordingly
     */
    public void getGameMessage() throws IOException, ClassNotFoundException
    {
        input = new ObjectInputStream(GameSocket.getInputStream());
        while(true) {
            msg = (Message) input.readUnshared();
            if (msg.getMsgType() == 1) {
                sendFleetCoordinates();
            } else if (msg.getMsgType() == 3) {
                printTables();
                sendBombLocation();
            }
            else if (msg.getMsgType() == 5){
                System.out.println(msg.getMsg());
                break;
            }
        }
    }

    /*
     * Sends coordinates of all ships to user
     */
    private void sendFleetCoordinates() throws IOException
    {
        //last thing to do
        msg.setMsgType(2);
        if(msg.getMsg() != null)
        {
            System.out.println("\nNOTICE:" + msg.getMsg());
        }
        clearMessage();
        printPTable("Ocean Grid:");
        insertAircraftCarrier(inputScanner);
        insertDestroyer(inputScanner);
        insertSubmarine(inputScanner);
        output.writeUnshared(msg);
    }

    /*
     * Asks user for aircraft carrier coordinates
     * @param scanner for user input
     */
    private void insertAircraftCarrier(Scanner inputScanner)
    {
        String playerInput;

        System.out.println("Enter the starting and adjacent coordinates of your first Aircraft Carrier separated by a space:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput + ",");

        System.out.println("Enter the starting and adjacent coordinates of your second Aircraft Carrier separated by a space:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput + ",");

    }

    /*
     * Asks user for destroyer coordinates
     * @param scanner for user input
     */
    private void insertDestroyer(Scanner inputScanner)
    {
        String playerInput;
        System.out.println("Enter the starting and adjacent coordinates of your first Destroyer separated by a space:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput + ",");

        System.out.println("Enter the starting and adjacent coordinates of your second Destroyer separated by a space:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput + ",");
    }

    /*
     * Asks user for submarine coordinates
     * @param scanner for user input
     */
    private void insertSubmarine(Scanner inputScanner)
    {
        String playerInput;
        System.out.println("Enter a single coordinate of where to deploy your first Submarine:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput + ",");

        System.out.println("Enter a single coordinate of where to deploy your second Submarine:");
        playerInput = inputScanner.nextLine().toUpperCase();
        msg.setMsg(msg.getMsg() + playerInput);
    }

    /*
     * Sends Bomb location from user input to server
     */
    private void sendBombLocation() throws IOException
    {
        while(true)
        {
            System.out.println("Enter the coordinates you would like to bomb:");
            clearMessage();
            playerInput = inputScanner.nextLine().toUpperCase();
            if (playerInput.length() == 2)
            {
                msg.setMsg(playerInput);
                msg.setMsgType(4);
                output.writeUnshared(msg);
                break;
            }
            else
            {
                System.out.println("Incorrect Input. Try Again.");
            }
        }
    }

    /*
     * Converts message's message to ""
     */
    private void clearMessage()
    {
        msg.setMsg("");
    }


    /*
     * Prints all the tables and messages from server
     */
    private void printTables()
    {
        if(msg.getMsg() != null)
        {
            System.out.println("\nNOTICE:" + msg.getMsg());
        }
        printFTable("Your Fleet:");
        System.out.println();
        printPTable("Locations You Have Bombed:");
    }

    /*
     * Prints PTable
     */
    private void printPTable(String tableMessage)
    {
        System.out.println(tableMessage);
        System.out.print(msg.Ptable.toString());
    }

    /*
     * Prints FTable
     */
    private void printFTable(String tableMessage)
    {
        System.out.println(tableMessage);
        System.out.println(msg.Ftable.toString());
    }

    // main to run client
    public static void main(String args[]) throws IOException, ClassNotFoundException
    {
        Client client = new Client();
        client.getGameMessage();
    }
}
