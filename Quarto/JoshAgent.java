import java.util.ArrayList;

public class JoshAgent extends QuartoAgent {
  int turn=0;
    int depth = 1;
    int pieceToPass=0;
    static int MIN_SCORE = -100;
    static int MAX_SCORE = 100;
    static int INFINITY = Integer.MAX_VALUE;
    //Example AI
    public JoshAgent(GameClient gameClient, String stateFileName) {
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
        JoshAgent quartoAgent = new JoshAgent(gameClient, stateFileName);
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

        if(turn > 9)
          depth = 3;
        else if(turn > 4)
          depth = 2;
        System.out.println("Depth: " + depth + "\n");

        long startTime = System.nanoTime();

        int spotCount = getRemainingSpotCount(this.quartoBoard);
        ArrayList<Integer> remPieces = getRemainingPieces(this.quartoBoard);
        ArrayList<int[]> remSpots = getRemainingSpots(this.quartoBoard);
        /*Start out by calculating the number of root and makes nodes for our tree.
          The reason we want the number of first layer options is to choose from later
          After we've returned our tree and calculated our evaluations recursively back to the root layer.
          In case it isn't clear, spot count represents the number of available spaces
        */
        Node[] rootLayer = new Node[spotCount];

        //Loop through each root node and build trees
        for (int i = 0; i < spotCount; i++){
          rootLayer[i] = MiniMax(true, pieceID, remSpots.get(i)[0], remSpots.get(i)[1], depth, remPieces, remSpots, this.quartoBoard, -INFINITY, INFINITY);
        }

        for (Node n :rootLayer){
          System.out.println(n.score);
        }
        //Root layer is now a list of nodes, each of which containing a score.
        //Choose the max of these nodes
        Node maxNode = rootLayer[0];
        for (int j = 1; j < rootLayer.length; j++){
          if(maxNode.score < rootLayer[j].score){
            maxNode = rootLayer[j];
          }
        }

        pieceToPass = maxNode.passedPiece;

        //Timer
        long endTime = System.nanoTime();
        System.out.println("\n" + (endTime-startTime)/1000000);

        turn+=1;
        //Return final choice
        return maxNode.row + "," + maxNode.col;
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
     int[] commonCharacteristics;
     int[] diagonalInCommon;
     int diagonalTemp = 0;

     diagonalInCommon = new int[] {0,0,0,0,0};

     // check rows
     for (int x = 0; x < qb.getNumberOfRows(); x++) {

       //Check for skip
       int temp = 0;
       for(int i = 0; i < qb.getNumberOfColumns(); i++){
         if(qb.isSpaceTaken(x, i)){
          temp++;
          if(x==i)
            diagonalTemp++;
        }
       }

       if(temp == 4){
         commonCharacteristics = new int[] {0, 0, 0, 0, 0};

         for (int y = 0; y < qb.getNumberOfColumns(); y++) {
           // Get the characteristic array of piece at [x, y], and [x, y+1]
           QuartoPiece current_piece = qb.getPieceOnPosition(x, y);
           if(current_piece == null)
              continue;
           // Get the characteristic arrays
           characteristics = current_piece.getCharacteristicsArray();
           // Check if these two arrays have a common feature
           for(int i = 0; i < commonCharacteristics.length; i++) {
               commonCharacteristics[i] += characteristics[i] ? 1 : 0;
               //Diagonal
               if(x==y){
                 diagonalInCommon[i] += characteristics[i] ? 1 : 0;
               }
           }
         }
         //Increment score if row is good
         for(int i = 0; i < commonCharacteristics.length; i++) {
           if (commonCharacteristics[i] == 4 || commonCharacteristics[i] == 0){
             score++;
             break;
           }
         }
       }
     }
     if(diagonalTemp == 4){
       for(int i = 0; i < diagonalInCommon.length; i++) {
         if (diagonalInCommon[i] == 4 || diagonalInCommon[i] == 0){
           score++;
           break;
         }
       }
     }
       // resets the common characteristics for the next row analyzed
       //commonCharacteristics = new int[] {0, 0, 0, 0, 0};
     diagonalInCommon = new int[] {0,0,0,0,0};
     diagonalTemp = 0;
     // check columns
     for (int x = 0; x < qb.getNumberOfColumns(); x++) {

       //Check for skip
       int temp = 0;
       for(int i = 0; i < qb.getNumberOfRows(); i++){
         if(qb.isSpaceTaken(i, x)){
          temp++;
          diagonalTemp++;
        }
       }
       if(temp == 4){
         commonCharacteristics = new int[] {0, 0, 0, 0, 0};

         for (int y = 0; y < qb.getNumberOfRows(); y++) {
           QuartoPiece current_piece = qb.getPieceOnPosition(x, y);
           if(current_piece == null)
              continue;
           // Get the characteristic array
           characteristics = current_piece.getCharacteristicsArray();
           // add to common char array
           for(int i = 0; i < commonCharacteristics.length; i++) {
             commonCharacteristics[i] += characteristics[i] ? 1 : 0;
               if(qb.getNumberOfRows()-x == y)
                 diagonalInCommon[i] += characteristics[i] ? 1 : 0;
           }
         }
         for(int i = 0; i < commonCharacteristics.length; i++) {
           if (commonCharacteristics[i] == 4 || commonCharacteristics[i] == 0){
             score++;
             break;
           }
         }
       }
     }
     if(diagonalTemp == 4){
       for(int i = 0; i < diagonalInCommon.length; i++) {
         if (diagonalInCommon[i] == 4 || diagonalInCommon[i] == 0){
           score++;
           break;
         }
       }
     }

     if (player) {
       return -score;
       }
     else {
       return score;
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

    //Get remaining number of spots
    private int getRemainingSpotCount(QuartoBoard qb){
      int temp = 0;
      for(int i = 0; i < qb.getNumberOfRows(); i++){
        for (int j = 0; j < qb.getNumberOfColumns(); j++) {
          if(qb.getPieceOnPosition(i,j) == null)
            temp++;
        }
      }
      return temp;
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


    private Node MiniMax(boolean player, int piece, int row, int col, int depth, ArrayList<Integer> piecesLeft, ArrayList<int[]> spotsLeft, QuartoBoard prevBoard, int alpha, int beta){
      Node node = new Node(piece, row, col, player);
      //Alpha and beta
      node.alpha = alpha;
      node.beta = beta;
      //Create new quarto board
      QuartoBoard newBoard = new QuartoBoard(prevBoard);
      newBoard.insertPieceOnBoard(row, col, piece);
      //Make shallow copries and update our arraylists
      ArrayList<Integer> myPiecesLeft = new ArrayList<>(piecesLeft);
      ArrayList<int[]> mySpotsLeft = new ArrayList<>(spotsLeft);
      myPiecesLeft.remove(new Integer(piece));
      mySpotsLeft.remove(new int[]{row,col});

      //If player or enemy has won the game
      if(checkIfGameIsWon(newBoard, row, col)){
        node.isLeafNode = true;
        if(player){
          node.score = MAX_SCORE;
        }
        else{
          node.score = MIN_SCORE;
        }
        return node;
      }

      //If board is full or we reach max depth, evaluate state of board
      if(depth == 0 || mySpotsLeft.size() == 0){
        node.isLeafNode = true;
        node.score = EvaluateBoardState(newBoard, player);
        return node;
      }

      //We're not a leaf node, so we add children to our node
      for(int i = 0; i < myPiecesLeft.size(); i++){
        //Piece score represents the min move per piece
        int pieceScore = (player ? INFINITY : -INFINITY);
        for(int j = 0; j < mySpotsLeft.size(); j++){
          //We choose min spot per piece if player. Max for enemy
          Node child = MiniMax(!player, myPiecesLeft.get(i), mySpotsLeft.get(j)[0], mySpotsLeft.get(j)[1], depth-1, myPiecesLeft, mySpotsLeft, newBoard, node.alpha, node.beta);
          node.addChildNode(child);
          //If player, update pieceScore to min(allSpots)
          if((pieceScore > child.score) && player){
            pieceScore = child.score;

            //Pruning min (even though we're player, this inner loop is a min choice for spot per piece)
            if(node.alpha >= pieceScore){
              break;
            }
          }
          //If enemy, update pieceScore to max(allSpots)
          else if ((pieceScore < child.score) && !player){
            pieceScore = child.score;
            //Pruning max
            if(pieceScore >= node.beta){
              break;
            }
          }
        }

        //Now that we have the pieceScore, update max/min node accordingly
        if(((node.score < pieceScore) && player) || ((node.score > pieceScore) && !player)){
          node.setScore(pieceScore);
          node.passedPiece = myPiecesLeft.get(i);
          //Pruning
          if(node.alpha >= node.beta){
            break;
          }
        }
      }

      return node;
    }


    public class Node{
      //Node should store if its me or the opponent (max or min), if it should have children (leaf),
      //What board state it represents (row, col, piece), and finally a score/value/worth.
      boolean isLeafNode = false;
      boolean isPlayer;
      int score = 0;
      int passedPiece;
      int row, col;
      int alpha;
      int beta;
      ArrayList<Node> children;

      public Node(int piece, int row, int col, boolean player){
        this.isPlayer = player;
        if(player){
          this.score = -INFINITY;
          this.alpha = -INFINITY;
          this.beta = INFINITY;
        }
        else{
          this.score = INFINITY;
          this.alpha = INFINITY;
          this.beta = -INFINITY;
        }
        this.passedPiece = piece;
        this.row = row;
        this.col = col;
        this.children = new ArrayList<Node>();
      }
      public void addChildNode(Node node){
        children.add(node);
      }

      public void setScore(int score){
        this.score = score;
        if(this.isPlayer)
          this.alpha = score;
        else
          this.beta = score;
      }

    }
}
