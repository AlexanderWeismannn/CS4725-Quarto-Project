import java.util.ArrayList;

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
                    }
                }
            }
        }

        // We know we can't win with this piece, so...
        // Where can we place it such that it gives us the most value?
        // We know how good a placement is based on the number of similarities in that row, column, and diagonal with other pieces.
        // So, we want to place our piece in a row such that this number is the highest it can be WITHOUT being in a place that
        // Our opponent can win next turn.
        // We can be sure that our opponent cannot win from this placement if the NONE of the row, column, or diagonal have 3 pieces
        // that share a similarity in them already. (That is, we're placing the first, second, third, or WINNING piece).
        //
        // Steps:
        // 1) For all empty spots:
        // 2) Place our piece in a copy of the board, and then check the row, column, and diagonal:
        // 3) If either the row, column, or diagonal now have 4 pieces in them (including our piece), reject this placement
        // 4) Else keep a note of how high our score is (based on how many similarities in the row/column/diagonal)
        // 5) Return the highest scoring placement
        // 6) On their turn, if we placed a piece such that a row/column/diagonal now has 3 pieces,
        //       Give the opponent the 4th piece in that sequence. This would give us the highest chance of winning on our next turn.
        //the high score value should hold an integer value along with the binary string and row / column




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
    // a highscore object that contains all relevant information for use
    // in the calculateMoveScore
    // one instance will be created and the pieceID, row, column will change based
    // on exploration of options

    public class HighScore{
        int score;
        int pieceID;
        int row;
        int column;

        public void setScore(int score){
            this.score = score;
        }

        public int getScore(){
            return score;
        }

        public void setPieceID(){
            this.pieceID = pieceID;
        }

        public int getPieceID(){
            return pieceID;
        }

        public void setRow(){
            this.row = row;
        }
        public int getRow(){
            return row;
        }

        public void setColumn(){
            this.column = column;
        }

        public int getColumn(){
            return column;
        }


    }


    /**
     * The method for determining a certain score that the piece has
     */
    private void calculateMoveScore(HighScore bestOption){

        // Steps:
        // 1) For all empty spots:
        // 2) Place our piece in a copy of the board, and then check the row, column, and diagonal:
        // 3) If either the row, column, or diagonal now have 4 pieces in them (including our piece), reject this placement
        // 4) Else keep a note of how high our score is (based on how many similarities in the row/column/diagonal)
        // 5) Return the highest scoring placement
        // 6) On their turn, if we placed a piece such that a row/column/diagonal now has 3 pieces,
        //       Give the opponent the 4th piece in that sequence. This would give us the highest chance of winning on our next turn.
        //the high score value should hold an integer value along with the binary string and row / column

        //we are assuming that there are no rows / colums / or diagonals with 4 pieces


    }



    private String testAllOptions(QuartoBoard currentBoard, int depth) {



        //depth 1 has already been explored by the move function
        //TODO:
        /**
         * 1.)Decide on a random move
         * 2.)See if there is a piece that you can give after that move that will not cause you to lose
         * 3.)if YES then continue to iterate until depth is reached / the end is reached
         *  -if depth is not reached in time for all results, store a running best move that has been explored to depth
         * 4.)if NO then choose another random move
         * 5.)
         */


    // Get a list of all available pieces

    ArrayList<Integer> pieces_we_can_use = new ArrayList<Integer>();

    for(int i = 0; i < currentBoard.pieces.length; i++) {
        if(!currentBoard.getPiece(i).isInPlay()) {
            pieces_we_can_use.add(i);
        }
    }

    // Now that we have all our potential pieces,
    //



//        //gets the next available piece in the piece array
//        public int chooseNextPieceNotPlayed() {
//            for(int i = 0; i < pieces.length; i++) {
//                if(!this.getPiece(i).isInPlay()) {
//                    return i;
//                }
//            }
//            //-1 should never be returned
//            return -1;
//        }






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
