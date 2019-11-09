import java.io.Serializable;

public class Battleship implements Serializable {

    private boolean isShipSunk;
    private int[] xCoordinates;
    private int[] yCoordinates;

    // constructor
    public Battleship(int shipSize)
    {
        isShipSunk = false;
        xCoordinates = new int[shipSize];
        yCoordinates = new int[shipSize];
    }

    // getters
    public boolean getIsShipSunk(){ return isShipSunk; }
    public int[] getXCoordinates() { return xCoordinates; }
    public int[] getYCoordinates() { return yCoordinates; }

    /*
     * Inserts "X" or "O" after dropping a bomb
     * @param X coordinate, Y coordinate, ship's nth coordinate
     */
    public void addCoordinates(int xCoordinate, int yCoordinate, int iteration)
    {
        xCoordinates[iteration] = xCoordinate;
        yCoordinates[iteration] = yCoordinate;
    }

    // setters
    public void setIsShipSunk(boolean isShipSunk) { this.isShipSunk = isShipSunk;}


}
