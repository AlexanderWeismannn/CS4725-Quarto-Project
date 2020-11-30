public class QuartoHopefullySomewhatSmartAgent extends QuartoAgent {

    //Example AI
    public QuartoHopefullySomewhatSmartAgent(GameClient gameClient, String stateFileName) {
        // because super calls one of the super class constructors(you can overload constructors), you need to pass the parameters required.
        super(gameClient, stateFileName);
    }

    //MAIN METHOD
    public static void main(String[] args) {
        //start the server
        GameClient gameClient = new GameClient();

        String ip = null;
        String stateFileName = null;
        //IP must be specified
        if(args.length > 0) {
            ip = args[0];
        } else {
            System.out.println("No IP Specified");
            System.exit(0);
        }
        if (args.length > 1) {
            stateFileName = args[1];
        }

        gameClient.connectToServer(ip, 4321);
        QuartoHopefullySomewhatSmartAgent quartoAgent = new QuartoHopefullySomewhatSmartAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();

    }


    /*
	 * This code will try to find a piece that the other player can't use to win immediately
	 */
    @Override
    protected String pieceSelectionAlgorithm() {

        //some useful lines:
        //String BinaryString = String.format("%5s", Integer.toBinaryString(pieceID)).replace(' ', '0');

        this.startTimer();
        boolean skip = false;
        for (int i = 0; i < this.quartoBoard.getNumberOfPieces(); i++) {
            skip = false;
            if (!this.quartoBoard.isPieceOnBoard(i)) {
                for (int row = 0; row < this.quartoBoard.getNumberOfRows(); row++) {
                    for (int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++) {
                        if (!this.quartoBoard.isSpaceTaken(row, col)) {
                            QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
                            copyBoard.insertPieceOnBoard(row, col, i);
                            if (copyBoard.checkRow(row) || copyBoard.checkColumn(col) || copyBoard.checkDiagonals()) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        break;
                    }

                }
                if (!skip) {
                    return String.format("%5s", Integer.toBinaryString(i)).replace(' ', '0');
                }

            }
            if (this.getMillisecondsFromTimer() > (this.timeLimitForResponse - COMMUNICATION_DELAY)) {
                //handle for when we are over some imposed time limit (make sure you account for communication delay)
            }
            String message = null;
            //for every other i, check if there is a missed message
            /*
            if (i % 2 == 0 && ((message = this.checkForMissedServerMessages()) != null)) {
                //the oldest missed message is stored in the variable message.
                //You can see if any more missed messages are in the socket by running this.checkForMissedServerMessages() again
            }
            */
        }


        //if we don't find a piece in the above code just grab the first random piece
        int pieceId = this.quartoBoard.chooseRandomPieceNotPlayed(100);
        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceId)).replace(' ', '0');


        return BinaryString;
    }

    /*
     * Do Your work here
     * The server expects a move in the form of:   row,column
     */
    @Override
    protected String moveSelectionAlgorithm(int pieceID) {

        /**
         *  This will hold [pieceID, row, col] in order of moves to win.
         *  If this is empty, it means we weren't able to find a winning sequence, so we'll have to
         *  Determine what to place in this scenario.
         *
         *  Example:
         *  System.out.println(bestSequenceOfMoves);
         *  > [[tall_square_no_hole_brown][row_2][column_3], [short_circle_hole_white][row_3][column_4] ... ]
         *
         *  Note: we'll have to make sure the sequence ends on our turn (so we don't help the opponent win...
         *
         *
         *  1. When it's our turn, and we are given a piece:
         *      For every empty tile, try our piece, check if we win
         *      If we don't win:
         *          For every empty tile, place our piece, then simulate the same process from the new board, with every available piece
         *          repeat
         */

        int bestSequenceOfMoves[][];

        /**
         * Checks for an immediate win condition, if it is true it returns that
         **/
        for(int row = 0; row < quartoBoard.getNumberOfRows(); row++){
            for(int col = 0; col < this.quartoBoard.getNumberOfColumns(); col++){
                //checks to see if space is taken
                if(this.quartoBoard.getPieceOnPosition(row,col) == null){
                    //if not make a duplicate board and fill in the empty space
                    QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
                    copyBoard.insertPieceOnBoard(row,col,pieceID);

                    //if the filled in space would lead to a won game, return the position
                    if(copyBoard.checkColumn(col) || copyBoard.checkRow(row) || copyBoard.checkDiagonals()){
                        return row + "," + col;
                    } else {
                        /**
                         * We didn't win on the first depth, now we need to explore further turns
                         * Depth is how many turns we'll look ahead. In this case, 3
                         */
                        testAllOptions(copyBoard, 3);
                    }
                }
            }
        }

        /**
         * Finally
         **/

        // If no winning move is found in the above code, then return a random (unoccupied) square
        //creates a move array of size 2
        int[] move = new int[2];
        QuartoBoard copyBoard = new QuartoBoard(this.quartoBoard);
        move = copyBoard.chooseRandomPositionNotPlayed(100);
        return move[0] + "," + move[1];
    }

    private String testAllOptions(QuartoBoard currentBoard, int depth) {

        //TODO:
        //1.) Implement some form of player vs other player check (ie is player 1 going or player 2)
        //2.) implement some method for checking the value fo the move based on a evaluation function


        //get bumped up based on the best possible move made
        // we will have a max value of 4
        // if 4 is achieved return that board position immediately
        // 0 - 3 are all other possible values
        //should implement a method for comparing and giving a return value
        int max = 0;

        //return the best value here
        //need to change it from null to something else
        if (depth <= 0) {
            return null;
        }


        /**
         * What this does (I think... )
         * For every empty cell,
         *  we try every single piece on that cell,
         *      and then simulate the next two turns given that placement
         *
         * What this ACTUALLY does:
         *  fuck knows
         */
        for(int row = 0; row < currentBoard.getNumberOfRows(); row++) {
            for (int col = 0; col < currentBoard.getNumberOfColumns(); col++) {
                //checks to see if space is taken
                if (currentBoard.getPieceOnPosition(row, col) == null) {
                    //if not, make a duplicate board and fill in the empty space with every possible piece
                    for (int i = 0; i < currentBoard.getNumberOfPieces(); i++) {
                        QuartoBoard copyBoard = new QuartoBoard(currentBoard);
                        copyBoard.insertPieceOnBoard(row, col, currentBoard.chooseNextPieceNotPlayed());

                        if (copyBoard.checkColumn(col) || copyBoard.checkRow(row) || copyBoard.checkDiagonals()) {
                            return row + "," + col;
                        } else {
                            testAllOptions(copyBoard, depth--);
                        }
                    }
                }
            }
        }

        return ("Ya dun fucked up");
    }


    //loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon() {

        //loop through rows
        for(int i = 0; i < NUMBER_OF_ROWS; i++) {
            //gameIsWon = this.quartoBoard.checkRow(i);
            if (this.quartoBoard.checkRow(i)) {
                System.out.println("Win via row: " + (i) + " (zero-indexed)");
                return true;
            }

        }
        //loop through columns
        for(int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            //gameIsWon = this.quartoBoard.checkColumn(i);
            if (this.quartoBoard.checkColumn(i)) {
                System.out.println("Win via column: " + (i) + " (zero-indexed)");
                return true;
            }

        }

        //check Diagonals
        if (this.quartoBoard.checkDiagonals()) {
            System.out.println("Win via diagonal");
            return true;
        }

        return false;
    }
}
