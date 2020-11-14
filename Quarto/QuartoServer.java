public class QuartoServer {

	private static final int NUMBER_OF_ROWS = 5;
	private static final int NUMBER_OF_COLUMNS = 5;
	private static final int NUMBER_OF_PIECES = 32;

	public static final String INFO_MESSAGE_HEADER = "INFO: ";

	public static final String ERROR_PIECE_HEADER = "ERR_PIECE: ";
	public static final String ERROR_MOVE_HEADER = "ERR_MOVE: ";

	public static final String ACKNOWLEDGMENT_PIECE_HEADER = "ACK_PIECE: ";
	public static final String ACKNOWLEDGMENT_MOVE_HEADER = "ACK_MOVE: ";

	public static final String PIECE_MESSAGE_HEADER = "PIECE: ";
	public static final String MOVE_MESSAGE_HEADER = "MOVE: ";

	public static final String SELECT_PIECE_HEADER = "Q1: ";
	public static final String SELECT_MOVE_HEADER = "Q2: ";
	public static final String GAME_OVER_HEADER = "GAME_OVER: ";
	public static final String TURN_TIME_LIMIT_HEADER = "TURN_TIME_LIMIT: ";

	//time limit is in milliseconds
	private static final int TIME_LIMIT_FOR_RESPONSE = 10000;

	GameServer gameServer;
	QuartoBoard quartoBoard;

	//The Main method
	public static void main(String[] args) {
		//start the server
		GameServer gameServer = new GameServer();
		String stateFileName = null;
		//optional pass in argument is the path to a .quarto file
		if(args.length > 0) {
			stateFileName = args[0];
		}
		//the server will keep running for additional games/clients
		while(true) {
			gameServer.startServer(4321);
			gameServer.acceptClients(2);
			QuartoServer quarto = new QuartoServer(gameServer, stateFileName);
			quarto.play();
			gameServer.closeServer();
		}

	}

	//Class constructor
	public QuartoServer(GameServer gameServer, String stateFileName) {
		this.gameServer = gameServer;
		this.quartoBoard = new QuartoBoard(NUMBER_OF_ROWS, NUMBER_OF_COLUMNS, NUMBER_OF_PIECES, stateFileName);
	}

	//main game loop
	public void play() {
		this.gameServer.writeToAllPlayers(TURN_TIME_LIMIT_HEADER + TIME_LIMIT_FOR_RESPONSE);

		int playerOne = 1, playerTwo = 2;
		int[] move;
		int pieceID = -1;

		while(true) {
			//print state of game
			this.quartoBoard.printBoardState();


//Uncomment the next 6 lines to force the server to wait for you to hit 'Enter' between moves
//try {
//        System.in.read();
//    } catch (Exception e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }


			//Get player 2 to choose player 1's piece
			pieceID = this.choosePiece(playerOne, playerTwo);


//Uncomment the next 6 lines to force the server to wait for you to hit 'Enter' between moves
//try {
//        System.in.read();
//    } catch (Exception e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }

			//get player 1 to make his/her move with the piece given to him/her
			move = this.chooseMove(playerOne, playerTwo, pieceID);

			//add piece to board
			this.quartoBoard.insertPieceOnBoard(move[0], move[1], pieceID);

			if(this.checkIfGameIsWon()) {
				this.gameServer.writeToAllPlayers(GAME_OVER_HEADER + "player " + playerOne + " wins");
				this.quartoBoard.printBoardState();
				break;
			} else {
				int temp = playerOne;
				playerOne = playerTwo;
				playerTwo = temp;
			}

			if (this.checkIfGameIsDraw()) {
				this.gameServer.writeToAllPlayers(GAME_OVER_HEADER + "Game is a draw");
				this.quartoBoard.printBoardState();
				break;
			}

		}
	
	}

	private int choosePiece(int playerOneID, int playerTwoID) {

		int pieceID;

		this.gameServer.writeToPlayer(playerTwoID, SELECT_PIECE_HEADER + "Please choose piece for player " + playerOneID);
		//listen for amount of time equal to TIME_LIMIT_FOR_RESPONSE
		String response = this.gameServer.listenToPlayer(playerTwoID, TIME_LIMIT_FOR_RESPONSE);

		try {

			if(response == null) {
				throw new IllegalArgumentException("INVALID");
			}

			pieceID = Integer.parseInt(response, 2);
			if(!this.isValidPiece(pieceID)) {
				throw new IllegalArgumentException("INVALID");
			}

			this.gameServer.writeToPlayer(playerTwoID, ACKNOWLEDGMENT_PIECE_HEADER + quartoBoard.getPiece(pieceID).binaryStringRepresentation());

		} catch (Exception e) {
				pieceID = quartoBoard.chooseRandomPieceNotPlayed(100);
				this.gameServer.writeToPlayer(playerTwoID, ERROR_PIECE_HEADER + quartoBoard.getPiece(pieceID).binaryStringRepresentation());
		}

		//line not needed anymore, keeping it here unless reverting code
		//this.gameServer.writeToPlayer(playerOneID, PIECE_MESSAGE_HEADER + quartoBoard.getPiece(pieceID).binaryStringRepresentation());
		return pieceID;

	}

	private int[] chooseMove(int playerOneID, int playerTwoID, int pieceID) {

		int[] move = new int[2];
		this.gameServer.writeToPlayer(playerOneID, SELECT_MOVE_HEADER + quartoBoard.getPiece(pieceID).binaryStringRepresentation() + " (please select move)");
		String response = this.gameServer.listenToPlayer(playerOneID, TIME_LIMIT_FOR_RESPONSE);

		try {
			if(response == null) {
				throw new IllegalArgumentException("INVALID");
			}
			String[] rowColumn = response.split(",");
			move[0] = Integer.parseInt(rowColumn[0]);
			move[1] = Integer.parseInt(rowColumn[1]);

			if(!this.isValidMove(move[0], move[1])) {
				throw new IllegalArgumentException("INVALID");
			}
			this.gameServer.writeToPlayer(playerOneID, ACKNOWLEDGMENT_MOVE_HEADER + move[0] + "," + move[1]);

		} catch (Exception e) {
			move = quartoBoard.chooseRandomPositionNotPlayed(100);
			this.gameServer.writeToPlayer(playerOneID, ERROR_MOVE_HEADER + move[0] + "," + move[1]);
		}
		this.gameServer.writeToPlayer(playerTwoID, MOVE_MESSAGE_HEADER + move[0] + "," + move[1]);

		return move;
	}

	private boolean isValidPiece(int pieceID) {
		//error checking
		if(pieceID < 0 || pieceID >= this.quartoBoard.getNumberOfPieces() || quartoBoard.isPieceOnBoard(pieceID)) {
			return false;
		}

		return true;
	}

	private boolean isValidMove(int row, int column) {
		//error checking
		if(row < 0 || row >= this.quartoBoard.getNumberOfRows() || column < 0 || column >= this.quartoBoard.getNumberOfColumns() || this.quartoBoard.isSpaceTaken(row, column)) {
			return false;
		}

		return true;
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

	//loop through board and see if the game is in a won state
	private boolean checkIfGameIsDraw() {
		return this.quartoBoard.checkIfBoardIsFull();
	}

}
