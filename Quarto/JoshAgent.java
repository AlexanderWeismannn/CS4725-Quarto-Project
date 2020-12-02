import java.util.ArrayList;

public class JoshAgent extends QuartoAgent {
  int turn=0;
    int depth = 1;
    int pieceToPass=0;
    static int MIN_SCORE = -100;
    static int MAX_SCORE = 100;
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

        // if(turn > 20)
        //   depth = 4;
        // else if(turn > 15)
        //   depth = 3;
        // else if(turn > 6)
        //   depth = 2;
        //System.out.println(depth);
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
          rootLayer[i] = MiniMax(true, pieceID, remSpots.get(i)[0], remSpots.get(i)[1], depth, remPieces, remSpots, this.quartoBoard);
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
        System.out.println((endTime-startTime)/1000000);

        turn++;
        //Return final choice
        return maxNode.row + "," + maxNode.col;
    }




    //Return a value representing whether a board is good or bad
    private int EvaluateBoardState(QuartoBoard qb, int row, int col, boolean player){
        return 0;
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


    private Node MiniMax(boolean player, int piece, int row, int col, int depth, ArrayList<Integer> piecesLeft, ArrayList<int[]> spotsLeft, QuartoBoard prevBoard){
      Node node = new Node(piece, row, col, player);
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
        if(player)
          node.score = MAX_SCORE;
        else
          node.score = MIN_SCORE;
        return node;
      }
      //If board is full or we reach max depth
      if(depth == 0 || mySpotsLeft.size() == 0){
        node.isLeafNode = true;
        node.score = EvaluateBoardState(newBoard, row, col, player);
        return node;
      }

      //We're not a leaf node, so we add children to our node
      for(int i = 0; i < myPiecesLeft.size(); i++){
        int pieceScore = (player ? MAX_SCORE : MIN_SCORE);
        for(int j = 0; j < mySpotsLeft.size(); j++){
          //We choose min spot per piece if player
          Node child = MiniMax(!player, myPiecesLeft.get(i), mySpotsLeft.get(j)[0], mySpotsLeft.get(j)[1], depth-1, myPiecesLeft, mySpotsLeft, newBoard);
          node.addChildNode(child);

          if(pieceScore > child.score && player)
              pieceScore = child.score;
          else if (pieceScore < child.score && !player)
            pieceScore = child.score;
        }

        if((node.score < pieceScore && player) || (node.score > pieceScore && !player)){
          node.score = pieceScore;
          node.passedPiece = myPiecesLeft.get(i);
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
      ArrayList<Node> children;

      public Node(int piece, int row, int col, boolean player){
        this.isPlayer = player;
        if(player)
          this.score = MIN_SCORE;
        else
          this.score = MAX_SCORE;
        this.passedPiece = piece;
        this.row = row;
        this.col = col;
        this.children = new ArrayList<Node>();
      }
      public void addChildNode(Node node){
        children.add(node);
      }

    }
}
