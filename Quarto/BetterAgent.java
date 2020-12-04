import java.util.ArrayList;
import java.util.Collections;

public class BetterAgent extends QuartoAgent {
    int depth = 1;
    int pieceToPass=0;
    static int MIN_SCORE = -100;
    static int MAX_SCORE = 100;
    static int INFINITY = Integer.MAX_VALUE;
    static int NegINFINITY = Integer.MIN_VALUE;
    //Example AI
    public BetterAgent(GameClient gameClient, String stateFileName) {
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
        BetterAgent quartoAgent = new BetterAgent(gameClient, stateFileName);
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

        long startTime = System.nanoTime();

        ArrayList<Integer> remPieces = getRemainingPieces(this.quartoBoard);
        ArrayList<int[]> remSpots = getRemainingSpots(this.quartoBoard);
        /*Start out by calculating the number of root and makes nodes for our tree.
          The reason we want the number of first layer options is to choose from later
          After we've returned our tree and calculated our evaluations recursively back to the root layer.
          In case it isn't clear, spot count represents the number of available spaces
        */
        int spotsLeft = remSpots.size();
        if(spotsLeft < 9)
          depth = 3;
        else if(spotsLeft < 22)
          depth = 2;

        System.out.println("Depth: " + depth + "\n");

        Node maxNode = MiniMaxMove(true, pieceID, remSpots, remPieces, this.quartoBoard, depth, NegINFINITY, INFINITY);

        pieceToPass = maxNode.passedPiece;

        //Timer
        long endTime = System.nanoTime();
        System.out.println("\n" + (endTime-startTime)/1000000);

        //Return final choice
        return maxNode.row + "," + maxNode.col;
    }

    public Node MiniMaxMove(boolean nodeType, int pieceID, ArrayList<int[]> emptySpots, ArrayList<Integer> freePieces, QuartoBoard qb, int depth, int alpha, int beta){
      //Piece nodes are given a piece to play. Its children are spots to play the piece
      Node pieceNode = new Node(nodeType, pieceID, alpha, beta);

      //We can immediately update the pieces available since we know we'll be playing pieceID
      ArrayList<Integer> newFreePieces = new ArrayList<>(freePieces);
      newFreePieces.remove(new Integer(pieceID));

      if(depth==3){
        int[] spotScore = new int[emptySpots.size()];
        //Sort the emptySpots to optimize Pruning
        for(int i=0; i < spotScore.length; i++){
          //Get evaluation of board and store int values
          QuartoBoard newBoard = new QuartoBoard(qb);
          newBoard.insertPieceOnBoard(emptySpots.get(i)[0], emptySpots.get(i)[1], pieceID);
          spotScore[i] = EvaluateBoardState(newBoard, nodeType);
        }
        //Loop through empty spots again, but this time sort them
        sort(spotScore, emptySpots, 0, spotScore.length-1);

        if(!nodeType){
          Collections.reverse(emptySpots);
        }
      }
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

    // //Get remaining number of pieces
    // private int getRemainingPieceCount(QuartoBoard qb){
    //   int temp = 0;
    //   for(int i = 0; i < qb.getNumberOfPieces(); i++){
    //       if(!qb.getPiece(i).isInPlay())
    //         temp++;
    //   }
    //   return temp;
    // }

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

    //Quicksort
    /* This function takes last element as pivot,
       places the pivot element at its correct
       position in sorted array, and places all
       smaller (smaller than pivot) to left of
       pivot and all greater elements to right
       of pivot */
       public int partition(int arr[], ArrayList<int[]> arrSpots, int low, int high)
       {
           int pivot = arr[high];
           int i = (low-1); // index of smaller element
           for (int j=low; j<high; j++)
           {
               // If current element is smaller than the pivot
               if (arr[j] < pivot)
               {
                   i++;
                   // swap arr[i] and arr[j]
                   int temp = arr[i];
                   int[] tempSpot = arrSpots.get(i);
                   arr[i] = arr[j];
                   arrSpots.set(i, arrSpots.get(j));
                   arr[j] = temp;
                   arrSpots.set(j, tempSpot);
               }
           }
           // swap arr[i+1] and arr[high] (or pivot)
           int temp = arr[i+1];
           int[] tempPos = arrSpots.get(i+1);
           arr[i+1] = arr[high];
           arrSpots.set(i+1, arrSpots.get(high));
           arr[high] = temp;
           arrSpots.set(high, tempPos);

           return i+1;
       }
       /* The main function that implements QuickSort()
         arr[] --> Array to be sorted,
         low  --> Starting index,
         high  --> Ending index */
       public void sort(int arr[], ArrayList<int[]> spotArr, int low, int high)
       {
           if (low < high)
           {
               /* pi is partitioning index, arr[pi] is
                 now at right place */
               int pi = partition(arr, spotArr, low, high);
               // Recursively sort elements before
               // partition and after partition
               sort(arr, spotArr, low, pi-1);
               sort(arr, spotArr, pi+1, high);
           }
        }
        //End of quicksort

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
