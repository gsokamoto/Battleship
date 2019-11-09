import java.io.*;
import java.net.*;
public class GameThread extends Thread {

    private Socket BattleShipSocket = null;
    private ObjectInputStream in	 = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in2	 = null;
    private ObjectOutputStream out2 = null;
    private BattleShipTable playerOneFTable;
    private BattleShipTable playerOnePTable;
    private BattleShipTable playerTwoFTable;
    private BattleShipTable playerTwoPTable;

    // constructor
    public GameThread(Socket s)
    {
        playerOnePTable = new BattleShipTable();
        BattleShipSocket = s;
        try {
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }
    }

    /*
     * Add player 2's default settings (similar to constructor)
     */
    public void addPlayer2(Socket s)
    {
        try {
            in2 = new ObjectInputStream(s.getInputStream());
            out2 = new ObjectOutputStream(s.getOutputStream());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }
        playerTwoPTable = new BattleShipTable();
        initializeGame(true);
    }

    /*
     * Allows first play to start initializing their board
     * @override
     */
    public void run()
    {
        initializeGame(false);

    }

    /*
     * Inserts "X" or "O" after dropping a bomb
     * @param player's socket, true if second player who connected
     */
    private void initializeGame(Boolean isPlayerTwo) {
        try {
            boolean isValidBoard = false;
                Message message = new Message();
                Message receivedMessage;
            while(!isValidBoard) {
                message.setMsgType(1);
                message.Ptable = new BattleShipTable(); // give empty board to show player this grid
                if (isPlayerTwo == false) {
                    receivedMessage = sendMessageToPlayerOne(message);
                } else {
                    receivedMessage = sendMessageToPlayerTwo(message);
                }

                /* DO ERROR CHECKING IF RETURN FALSE SEND MESSAGE WITH TYPE 1 */
                if (receivedMessage.getMsgType() == 2) {
                    isValidBoard = generateBoard(receivedMessage);
                    if(!isValidBoard)
                    {
                        message = new Message();
                        message.setMsg("There was at least one invalid input. Try Again.\n");
                    }
                    if (playerOneFTable != null && isPlayerTwo == false) {
                        try{
                            while(true) {
                                Thread.sleep(1000);
                                if (playerTwoFTable != null) {
                                    startGame();
                                    break;
                                }
                            }
                        } catch (InterruptedException ex) {
                            System.out.print(ex.getMessage());
                        }
                    }
                }
            }
        }catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        } catch(ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /*
     * Generates board from player's inputs
     * @return true if board was successfully generated
     * @param message from server
     */
    private boolean generateBoard(Message msg)
    {
        try {
            BattleShipTable battleShipTable = new BattleShipTable();
            String userCoordinates = msg.getMsg();
            String[] userCoordinatesParsed = userCoordinates.split(",");
            boolean isSuccess;
            for (int i = 0; i < 6; i++) {
                if (i < 2) {
                    String[] coordinateTable = userCoordinatesParsed[i].split(" ");
                    isSuccess = battleShipTable.insertAirCarrier(coordinateTable[0], coordinateTable[1]);
                } else if (i >= 2 && i < 4) {
                    String[] coordinateTable = userCoordinatesParsed[i].split(" ");
                    isSuccess = battleShipTable.insertDestroyer(coordinateTable[0], coordinateTable[1]);
                } else {
                    isSuccess = battleShipTable.insertSubmarine(userCoordinatesParsed[i]);
                }
                if (isSuccess == false) {
                    return false;
                }
            }

            if(playerOneFTable == null)
            {
                playerOneFTable = battleShipTable;
            }
            else
            {
                playerTwoFTable = battleShipTable;
            }
        }catch(ArrayIndexOutOfBoundsException ex)
        {
            return false;
        }catch(NumberFormatException ex)
        {
            return false;
        }
        return true;
    }

    /*
     * Takes turn of each player 1 by 1 until game is over
     * Thread ends when game is over
     */
    private void startGame() throws IOException, ClassNotFoundException
    {
        Message message = new Message();
        while(true) {
            playerActionMessage(false);
            if(playerTwoFTable.isGG()) {
                message.setMsgType(5);
                message.setMsg("GAME OVER\nYOU LOSE");
                out2.writeUnshared(message);
                message.setMsg("GAME OVER\nYOU WIN");
                out.writeUnshared(message);
                break;
            }
            playerActionMessage(true);
            if(playerOneFTable.isGG()) {
                message.setMsgType(5);
                message.setMsg("GAME OVER\nYOU WIN");
                out2.writeUnshared(message);
                message.setMsg("GAME OVER\nYOU LOSE");
                out.writeUnshared(message);
                break;
            }
        }
    }

    /*
     * Send bombs location provided by the player
     * @param true if player 2's turn
     */
    private void playerActionMessage(Boolean isPlayerTwo) throws IOException, ClassNotFoundException
    {
        Message message = new Message();
        boolean isSuccess = false;
        while(!isSuccess) {

            message.setMsgType(3);
            Message receivedMessage;
            if (isPlayerTwo == false) {
                message.Ftable = playerOneFTable;
                message.Ptable = playerOnePTable;
                if (playerTwoFTable.checkShipsDestroyed(false) && message.getMsg() == null) {
                    message.setMsg("Enemy ship sunk!\n");
                }
                receivedMessage = sendMessageToPlayerOne(message);
                isSuccess = playerTwoFTable.insertBomb(receivedMessage.getMsg());
                if(isSuccess)
                    playerOnePTable.insertHit(receivedMessage.getMsg(), playerTwoFTable.recentlyBombed);
                else {
                    message = new Message();
                    message.setMsg("Invalid location to bomb. Try Again.\n");
                }
            }
            else {
                message.Ftable = playerTwoFTable;
                message.Ptable = playerTwoPTable;
                if (playerOneFTable.checkShipsDestroyed(false) && message.getMsg() == null) {
                    message.setMsg("Enemy ship sunk!\n");
                }
                receivedMessage = sendMessageToPlayerTwo(message);
                isSuccess = playerOneFTable.insertBomb(receivedMessage.getMsg());
                if(isSuccess)
                    playerTwoPTable.insertHit(receivedMessage.getMsg(), playerOneFTable.recentlyBombed);
                else {
                    message = new Message();
                    message.setMsg("Invalid location to bomb. Try Again.\n");
                }
            }
        }
    }

    /*
     * Sends message to player one
     * @return message received from player
     * @param message to send to player
     */
    private Message sendMessageToPlayerOne(Message message) throws IOException, ClassNotFoundException
    {
        //out.reset();
        out.writeUnshared(message);
        out.reset();
        return (Message) in.readUnshared();
    }

    /*
     * Sends message to player two
     * @return message received from player
     * @param message to send to player
     */
    private Message sendMessageToPlayerTwo(Message message) throws IOException, ClassNotFoundException
    {
        //out2.reset();
        out2.writeUnshared(message);
        out2.reset();
        return (Message) in2.readUnshared();
    }
}
