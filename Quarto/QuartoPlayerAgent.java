import java.util.ArrayList;

public class QuartoPlayerAgent extends QuartoAgent {
  int turn=0;
    int depth = 1;
    int pieceToPass=0;
    static int MIN_SCORE = -100;
    static int MAX_SCORE = 100;
    static int INFINITY = Integer.MAX_VALUE;
    static int NegINFINITY = Integer.MIN_VALUE;
    //Example AI
    public QuartoPlayerAgent(GameClient gameClient, String stateFileName) {
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
        QuartoPlayerAgent quartoAgent = new QuartoPlayerAgent(gameClient, stateFileName);
        quartoAgent.play();

        gameClient.closeConnection();

    }



    /*
     * This code will try to find a piece that the other player can't use to win immediately
     */
    @Override
    protected String pieceSelectionAlgorithm() {

        String BinaryString = String.format("%5s", Integer.toBinaryString(pieceToPass)).replace(' ', '0');

        return BinaryString;
    }


    @Override
    protected String moveSelectionAlgorithm(int pieceID) {

        ArrayList<Integer> remPieces = getRemainingPieces(this.quartoBoard);
        ArrayList<int[]> remSpots = getRemainingSpots(this.quartoBoard);
        /*Start out by calculating the number of root and makes nodes for our tree.
          The reason we want the number of first layer options is to choose from later
          After we've returned our tree and calculated our evaluations recursively back to the root layer.
          In case it isn't clear, spot count represents the number of available spaces
        */
        int remSpotCount = remSpots.size();
        if(remSpotCount < 9)
          depth = 3;
        else if(remSpotCount < 21)
          depth = 2;


        Node maxNode = MiniMaxMove(true, pieceID, remSpots, remPieces, this.quartoBoard, depth, NegINFINITY, INFINITY);

        pieceToPass = maxNode.passedPiece;

        //Return final choice
        return maxNode.row + "," + maxNode.col;
    }

    public Node MiniMaxMove(boolean nodeType, int pieceID, ArrayList<int[]> emptySpots, ArrayList<Integer> freePieces, QuartoBoard qb, int depth, int alpha, int beta){
      //Piece nodes are given a piece to play. Its children are spots to play the piece
      Node pieceNode = new Node(nodeType, pieceID, alpha, beta);

      //We can immediately update the pieces available since we know we'll be playing pieceID
      ArrayList<Integer> newFreePieces = new ArrayList<>(freePieces);
      newFreePieces.remove(new Integer(pieceID));


      //Sort the emptySpots to optimize Pruning
      // for(int i=0; i < emptySpots.size(); i++){
      //   //Get evaluation of board and sort array from best to worst
      //   QuartoBoard newBoard = new QuartoBoard(qb);
      //   newBoard.insertPieceOnBoard(row, col, pieceID);
      //   tempScore = EvaluateBoardState(newBoard, nodeType);
      // }

      //If there are spots left on the board, simulate playing your piece there
      for(int i=0; i < emptySpots.size(); i++){
        //Create new quarto board (this is the move)
        int row = emptySpots.get(i)[0];
        int col = emptySpots.get(i)[1];
        QuartoBoard newBoard = new QuartoBoard(qb);
        newBoard.insertPieceOnBoard(row, col, pieceID);
        //Make shallow copies and update our arraylists
        ArrayList<int[]> newEmptySpots = new ArrayList<>(emptySpots);
        newEmptySpots.remove(new int[]{row,col});

        //Move nodes inherit the parents piece, and also contain a unique location for that piece
        Node spotNode = MiniMaxPlay(nodeType, pieceID, row, col, newEmptySpots, newFreePieces, newBoard, depth, pieceNode.alpha, pieceNode.beta);
        //This node stems from the pieceNode
        pieceNode.addChildNode(spotNode);
        //Update our choice of move if it is better than our current one
        //We also update the best piece to give our opponent.
        //Of course, its possible we couldn't simulate any further and the piece hasn't changed
        if(nodeType && (spotNode.value > pieceNode.value)){
          pieceNode.value = spotNode.value;
          pieceNode.passedPiece = spotNode.passedPiece;
          pieceNode.row = spotNode.row;
          pieceNode.col = spotNode.col;
          pieceNode.alpha = spotNode.value;
          //Prune
          if(pieceNode.alpha >= pieceNode.beta){
           break;
          }
        }
        else if (!nodeType && (spotNode.value < pieceNode.value)){
          pieceNode.value = spotNode.value;
          pieceNode.passedPiece = spotNode.passedPiece;
          pieceNode.row = spotNode.row;
          pieceNode.col = spotNode.col;
          pieceNode.beta = spotNode.value;
          //Prune
          if(pieceNode.alpha >= pieceNode.beta){
            break;
          }
        }

      }

      return pieceNode;
    }

    public Node MiniMaxPlay(boolean nodeType, int pieceID, int row, int col, ArrayList<int[]> emptySpots, ArrayList<Integer> freePieces, QuartoBoard qb, int depth, int alpha, int beta){
      Node spotNode = new Node(nodeType, row, col, pieceID, alpha, beta);
      //In this method, we have played a piece (pieceID) in a position, (row col)
      //If we have reached our depth, we can't figure out what piece will benefit us most past this
      //However there's no need to figure it out if we win from this move
      //If the piece we just played won the game, we return this move
      if(checkIfGameIsWon(qb, row, col)){
        if(nodeType)
          spotNode.value = MAX_SCORE;
        else
          spotNode.value = MIN_SCORE;
        return spotNode;
      }
      //If the board is completely full, and we haven't won, we drew
      if(emptySpots.size() == 0){
        spotNode.value = 0;
        return spotNode;
      }
      //If we can't search anymore, evaluate the current baord and return
      if(depth == 0){
        spotNode.value = EvaluateBoardState(qb, nodeType);
        return spotNode;
      }

      //We haven't reached our depth so we can simulate further with the remaining pieces
      //Loop through potential pieces
      for(int i = 0; i < freePieces.size(); i++){
        //pieceNode will check all possible spots for the piece it was given and return the best choice
        Node pieceNode = MiniMaxMove(!nodeType, freePieces.get(i), emptySpots, freePieces, qb, depth-1, spotNode.alpha, spotNode.beta);
        spotNode.addChildNode(pieceNode);

        //We update our pieceNode's value based on min/max and child value
        if(nodeType && (pieceNode.value > spotNode.value)){
          spotNode.value = pieceNode.value;
          spotNode.passedPiece = freePieces.get(i);
          spotNode.alpha = pieceNode.value;
          //Prune
          if(spotNode.alpha >= spotNode.beta){
            break;
          }
        }
        else if(!nodeType && (pieceNode.value < spotNode.value)){
          spotNode.value = pieceNode.value;
          spotNode.passedPiece = freePieces.get(i);
          spotNode.beta = pieceNode.value;
          // Prune
          if(spotNode.alpha >= spotNode.beta){
            break;
          }
        }
      }

      return spotNode;
    }


    //Return a value representing whether a board is good or bad
    public int EvaluateBoardState(QuartoBoard qb, boolean player) {
     /* We need to:
        - Loop through every row, and note down each row that has 4 pieces in a row, with common characteristics
        - Loop through every column, and note down each column that has 4 pieces in a row, with common characteristics
        - Check the two diagonals, and note down each diagonal that has 4 pieces in a row, with common characteristics
    */
    int score = 0;
    boolean[] characteristics;
    int[] columnCheck;
    int[][] diagonalCheck = new int[2][5];
    int[] diagonalSkip = new int[2];
    int[][] rowCheck = new int[6][5];
    int columnSkip = 0;

     // check rows and columns
     for (int x = 0; x < qb.getNumberOfRows(); x++) {
         columnCheck = new int[] {0, 0, 0, 0, 0};
         columnSkip = 0;

         for (int y = 0; y < qb.getNumberOfColumns(); y++) {
           // Get the characteristic array of piece at [x, y], and [x, y+1]
           QuartoPiece current_piece = qb.getPieceOnPosition(x, y);
           if(current_piece == null){
             columnSkip++;
             rowCheck[5][y]++;
             if(x==y){
               diagonalSkip[0]++;
             }
             if(qb.getNumberOfRows()-x == y){
               diagonalSkip[1]++;
             }
              continue;
           }
           // Get the characteristic arrays
           characteristics = current_piece.getCharacteristicsArray();
           // Check if these two arrays have a common feature
           for(int i = 0; i < columnCheck.length; i++) {
               columnCheck[i] += characteristics[i] ? 1 : 0;
               rowCheck[y][i] = columnCheck[i];
               //Diagonal
               if(x==y){
                 diagonalCheck[0][i] += characteristics[i] ? 1 : 0;
               }
               if(qb.getNumberOfRows()-x == y){
                 diagonalCheck[1][i] += characteristics[i] ? 1 : 0;
               }
           }
         }
       if(columnSkip < 2){
         //Increment score if row is good
         for(int i = 0; i < columnCheck.length; i++) {
           if (columnCheck[i] == 4 || columnCheck[i] == 0){
             score++;
             break;
           }
         }
       }
     }
     //Check rows and diagonals
     for(int i = 0; i < rowCheck.length-1; i++){
       if(rowCheck[5][i] < 2){
         for(int j = 0; j < rowCheck[i].length; j++){
           if (rowCheck[i][j] == 4 || rowCheck[i][j] == 0){
             score++;
             break;
           }
         }
       }
     }
     for(int i = 0; i < diagonalCheck.length; i++){
       if(diagonalSkip[i] < 2){
         for (int j = 0; j < diagonalCheck[i].length; j++){
           if (diagonalCheck[0][i] == 4 || diagonalCheck[0][i] == 0){
             score++;
             break;
           }
         }
       }
     }

     if (player) {
       return score;
       }
     else {
       return -score;
     }
   }

    //loop through board and see if the game is in a won state
    private boolean checkIfGameIsWon(QuartoBoard qb, int row, int col) {
      if(qb.checkColumn(col) || qb.checkRow(row) || qb.checkDiagonals()){
          //we are in a win state so return 100
          return true;
      }
      return false;
    }

    //Get all the remining spots in [row, col] form
    private ArrayList<int[]> getRemainingSpots(QuartoBoard qb){
      ArrayList<int[]> tempSpots = new ArrayList<int[]>();
      for(int i = 0; i < qb.getNumberOfRows(); i++){
        for (int j = 0; j < qb.getNumberOfColumns(); j++) {
          if(qb.getPieceOnPosition(i,j) == null)
            tempSpots.add( new int[]{i, j} );
        }
      }
      return tempSpots;
    }

    //Get all the remaining pieces off board
    private ArrayList<Integer> getRemainingPieces(QuartoBoard qb){
      ArrayList<Integer> tempPieces = new ArrayList<>();
      for(int i = 0; i < qb.getNumberOfPieces(); i++){
        if(!qb.isPieceOnBoard(i))
          tempPieces.add(i);
      }
      return tempPieces;
    }


    public class Node{
      //Node should store if its me or the opponent (max or min), if it should have children (leaf),
      //What board state it represents (row, col, piece), and finally a score/value/worth.
      boolean isLeafNode = false;
      boolean nodeType;
      int value = 0;
      int passedPiece;
      int row, col;
      int alpha;
      int beta;
      ArrayList<Node> children;

      public Node(boolean nodeType, int row, int col, int pieceID, int alpha, int beta){
        this.nodeType = nodeType;
        this.row = row;
        this.col = col;
        this.passedPiece = pieceID;
        this.alpha = alpha;
        this.beta = beta;
        this.children = new ArrayList<Node>();

        //If max node, value initializes to -INFINITY
        if(nodeType){
          this.value = NegINFINITY;
        }
        else{
          this.value = INFINITY;
        }
      }

      public Node(boolean nodeType, int pieceID, int alpha, int beta){
        this.nodeType = nodeType;
        this.passedPiece = pieceID;
        this.alpha = alpha;
        this.beta = beta;
        this.children = new ArrayList<Node>();
        //If max node, value initializes to -INFINITY
        if(nodeType)
          this.value = NegINFINITY;
        else
          this.value = INFINITY;
      }

      public void addChildNode(Node node){
        children.add(node);
      }

    }
}
