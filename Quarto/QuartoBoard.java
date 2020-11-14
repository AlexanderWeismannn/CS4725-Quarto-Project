import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

public class QuartoBoard {

	public QuartoPiece[][] board;
	public QuartoPiece[] pieces;

	public QuartoBoard(int numberOfRows, int numberOfColumns, int numberOfPieces, String stateFileName) {
		//create board
		this.board = new QuartoPiece[numberOfRows][numberOfColumns];

		//create all QuartoPieces
		this.pieces = new QuartoPiece[numberOfPieces];
		for(int i = 0; i < numberOfPieces; i++) {
			this.pieces[i] = new QuartoPiece(i);
		}

		if (stateFileName != null) {
			this.setBoardFromFile(stateFileName, numberOfRows, numberOfColumns);
		}

	}

	//copy constructor that can be used by agents to get a "deep copy" of the board
	public QuartoBoard(QuartoBoard quartoBoard) {
		this.board = new QuartoPiece[quartoBoard.getNumberOfRows()][quartoBoard.getNumberOfColumns()];
		this.pieces = new QuartoPiece[quartoBoard.getNumberOfPieces()];

		for(int i = 0; i < this.getNumberOfPieces(); i++) {
			this.pieces[i] = new QuartoPiece(quartoBoard.pieces[i]);
		}

		for (int row = 0; row < quartoBoard.getNumberOfRows(); row++) {
			for (int col = 0; col < quartoBoard.getNumberOfColumns(); col++) {
				if (quartoBoard.getPieceOnPosition(row, col) != null) {
					this.board[row][col] = this.getPiece(quartoBoard.getPieceOnPosition(row, col).getPieceID());
				}
			}
		}

	}

	//returns a QuartoPiece object for the pieceId
	public QuartoPiece getPiece(int pieceID) {
		if(pieceID < 0 || pieceID >= this.getNumberOfPieces()) {
			return null;
		}
		return this.pieces[pieceID];
	}

	//returns a QuartoPiece object occupying [row,column]
	public QuartoPiece getPieceOnPosition(int row, int column) {

		if(row < 0 || row >= this.getNumberOfRows() || column < 0 || column >= this.getNumberOfColumns()) {
			return null;
		}

		return this.board[row][column];

	}

	//inserts a QuartoPiece object on the board
	public boolean insertPieceOnBoard(int row, int column, int pieceID) {

		//some error checking
		if(row < 0 || row >= this.getNumberOfRows() || column < 0 || column >= this.getNumberOfColumns() || pieceID < 0 || pieceID >= this.getNumberOfPieces() || this.getPiece(pieceID).isInPlay()) {
			return false;
		}

		//update the piece's info
		this.getPiece(pieceID).setPosition(row, column);
		//update the board
		this.board[row][column] = this.getPiece(pieceID);

		return true;

	}

	//checks if a piece is on the board
	public boolean isPieceOnBoard(int pieceID) {
		QuartoPiece piece = this.getPiece(pieceID);
		if(piece == null) {
			return false;
		}
		return piece.isInPlay();
	}	

	//checks if a space is occupied
	public boolean isSpaceTaken(int row, int column) {
		if(row < 0 || row >= this.getNumberOfRows() || column < 0 || column >= this.getNumberOfColumns() || this.getPieceOnPosition(row, column) == null ) {
			return false;
		}
		return true;

	}

	//simple Getters
	public int getNumberOfRows() {
		return this.board.length;
	}
	
	public int getNumberOfColumns() {
		return this.board[0].length;
	}

	public int getNumberOfPieces() {
		return this.pieces.length;
	}


	//checks to see if there is a winning row
	public boolean checkRow(int row) {

		boolean[] characteristics;
		int[] commonCharacteristics = new int[] {0, 0, 0, 0, 0};

		for(int column = 0; column < this.getNumberOfColumns(); column++) {
			QuartoPiece piece = this.getPieceOnPosition(row, column);
			if(piece == null) {
				return false;
			}
			characteristics = piece.getCharacteristicsArray();
			for(int i = 0; i < commonCharacteristics.length; i++) {
				commonCharacteristics[i] = commonCharacteristics[i] + (characteristics[i] ? 1 : 0);
			}
		}

		//loop through the commonCharacteristics array
		//if the value is either 0 or 5 for any commonCharacteristics[i], all 5 pieces share that characteristic
		for(int i = 0; i < commonCharacteristics.length; i++) {
			if (commonCharacteristics[i] == 0 || commonCharacteristics[i] == 5) {
				return true;
			}
		}

		return false;

	}

	//checks to see if there is a winning column
	public boolean checkColumn(int column) {

		boolean[] characteristics;
		int[] commonCharacteristics = new int[] {0, 0, 0, 0, 0};

		for(int row = 0; row < this.getNumberOfRows(); row++) {
			QuartoPiece piece = this.getPieceOnPosition(row, column);
			if(piece == null) {
				return false;
			}
			characteristics = piece.getCharacteristicsArray();
			for(int i = 0; i < commonCharacteristics.length; i++) {
				commonCharacteristics[i] = commonCharacteristics[i] + (characteristics[i] ? 1 : 0);
			}
		}

		//loop through the commonCharacteristics array
		//if the value is either 0 or 5 for any commonCharacteristics[i], all 5 pieces share that characteristic
		for(int i = 0; i < commonCharacteristics.length; i++) {
			if (commonCharacteristics[i] == 0 || commonCharacteristics[i] == 5) {
				return true;
			}
		}

		return false;
	}

	//checks the Diagonals
	public boolean checkDiagonals() {
		boolean[] characteristics;
		int[] commonCharacteristics = new int[] {0, 0, 0, 0, 0};
		boolean unableToWinFirstDiagonal = false;


		for(int row = 0, column = 0; row < this.getNumberOfRows(); row++, column++) {
			QuartoPiece piece = this.getPieceOnPosition(row, column);
			if(piece == null) {
				unableToWinFirstDiagonal = true;
				break;
			}
			characteristics = piece.getCharacteristicsArray();
			for(int i = 0; i < commonCharacteristics.length; i++) {
				commonCharacteristics[i] = commonCharacteristics[i] + (characteristics[i] ? 1 : 0);
			}
		}

		if (!unableToWinFirstDiagonal) {
			for(int i = 0; i < commonCharacteristics.length; i++) {
				if (commonCharacteristics[i] == 0 || commonCharacteristics[i] == 5) {
					return true;
				}
			}
		}

		commonCharacteristics = new int[] {0, 0, 0, 0, 0};

		for(int row = this.board.length - 1, column = 0; row >= 0; row--, column++) {
			QuartoPiece piece = this.getPieceOnPosition(row, column);
			if(piece == null) {
				return false;
			}
			characteristics = piece.getCharacteristicsArray();
			for(int i = 0; i < commonCharacteristics.length; i++) {
				commonCharacteristics[i] = commonCharacteristics[i] + (characteristics[i] ? 1 : 0);
			}

		}

		for(int i = 0; i < commonCharacteristics.length; i++) {
			if (commonCharacteristics[i] == 0 || commonCharacteristics[i] == 5) {
				return true;
			}
		}

		return false;
	}

	//gets the next available piece in the piece array
	public int chooseNextPieceNotPlayed() {
		for(int i = 0; i < pieces.length; i++) {
			if(!this.getPiece(i).isInPlay()) {
				return i;
			}
		}
		//-1 should never be returned
		return -1;
	}


	//overloaded so you can pass an offset
	public int chooseNextPieceNotPlayed(int offset) {
		for(int i = offset; i < pieces.length; i++) {
			if(!this.getPiece(i).isInPlay()) {
				return i;
			}
		}
		//-1 should never be returned
		return -1;
	}

	//gets the next space on the board not occupied
	public int[] chooseNextPositionNotPlayed() {
		for(int row = 0; row < this.getNumberOfRows(); row++) {
			for(int column = 0; column < this.getNumberOfColumns(); column++) {
				if(this.getPieceOnPosition(row, column) == null) {
					return new int[] {row, column};
				}
			}
		}
		//-1,-1 is only returned if there are no positions left
		return new int[] {-1, -1};
	}


	//tries to get a random piece not played up to a certain number of attempts
	public int chooseRandomPieceNotPlayed(int numberOfAttempts) {
		//if used up all random attemps, try to just choose the next piece
		if(numberOfAttempts == 0) {
			return this.chooseNextPieceNotPlayed();
		}

		//generate random number
		int pieceId = (int)(Math.random() * (this.getNumberOfPieces()));

		if(this.getPiece(pieceId).isInPlay()) {
			pieceId = this.chooseRandomPieceNotPlayed(numberOfAttempts - 1);
		}

		return pieceId;

	}

	//tries to get a random position up to a certain number of attempts
	public int[] chooseRandomPositionNotPlayed(int numberOfAttempts) {

		if(numberOfAttempts == 0) {
			return this.chooseNextPositionNotPlayed();
		}

		int[] move = new int[2];

		move[0] = (int)(Math.random() * (this.getNumberOfRows()));
		move[1] = (int)(Math.random() * (this.getNumberOfColumns()));

		if(this.getPieceOnPosition(move[0], move[1]) != null) {
			move = this.chooseRandomPositionNotPlayed(numberOfAttempts - 1);
		}

		//-1,-1 is only returned if there are no positions left
		return move;
	}

	//prints the board out
	public void printBoardState() {
		System.out.println("-----------------------------------");
		for(int row = 0; row < this.getNumberOfRows(); row++) {
			for(int column = 0; column < this.getNumberOfColumns(); column++) {
				if(this.getPieceOnPosition(row, column) != null) {
					System.out.print(this.getPieceOnPosition(row, column).binaryStringRepresentation() + "  ");
				}
				else {
					System.out.print("null   ");
				}
				
			}
			System.out.print("\n");
		}
		System.out.println("-----------------------------------");
	}


	public void setBoardFromFile(String stateFileName, int numberOfRows, int numberOfColumns) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(stateFileName));
			String line;
			int row = 0;
			while ((line = br.readLine()) != null) {
				String[] splitted = line.split("\\s+");
				if (splitted.length != numberOfRows) {
					throw new Error("malformed .quarto file");
				}

				for (int col = 0; col < splitted.length; col++) {
					if (!splitted[col].equals("null")) {

						int pieceID = Integer.parseInt(splitted[col], 2);

						if (!insertPieceOnBoard(row, col, pieceID)) {
							throw new Error("malformed .quarto file");
						}
					}
				}


				row++;
			}
			br.close();

			for(int i = 0; i < numberOfRows; i++) {
				//gameIsWon = this.quartoBoard.checkRow(i);
				if (this.checkRow(i)) {
					System.out.println("Win via row: " + (i));
					throw new Error(".quarto file is not formatted correctly");
				}

			}
			//loop through columns
			for(int i = 0; i < numberOfColumns; i++) {
				//gameIsWon = this.quartoBoard.checkColumn(i);
				if (this.checkColumn(i)) {
					System.out.println("Win via column: " + (i));
					throw new Error(".quarto file is not formatted correctly");
				}

			}

			//check Diagonals
			if (this.checkDiagonals()) {
				System.out.println("Win via diagonal");
				throw new Error(".quarto file is not formatted correctly");
			}

		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error parsing quarto File");
			System.exit(0);
		}
	}

	//checks if the board is full
	public boolean checkIfBoardIsFull() {
		for(int row = 0; row < this.getNumberOfRows(); row++) {
			for(int column = 0; column < this.getNumberOfColumns(); column++) {
				if (board[row][column] == null) {
					return false;
				}
			}
		}
		return true;
	}

}
