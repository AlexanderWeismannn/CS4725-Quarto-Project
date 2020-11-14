public class QuartoPiece {

	private int pieceID;

	private boolean[] characteristics;
	//first position [0] is tall/short
	//second position [1] is solid/hollow
	//third position [2] is white/black
	//fourth position [3] is wood/metal
	//fifth position [4] is round/square


	private int row, column;
	private boolean inPlay;

	public QuartoPiece(int pieceID) {
		this.inPlay = false;
		this.pieceID = pieceID;
		//piece does not have a position upon creation, so set row and column to -1
		row = -1;
		column = -1;

		this.characteristics = new boolean[5];
		this.setCharacteristics();

	}

	//copy constructor
	public QuartoPiece(QuartoPiece quartoPiece) {
		this.inPlay = quartoPiece.isInPlay();
		this.pieceID = quartoPiece.getPieceID();
		this.row = quartoPiece.getRow();
		this.column = quartoPiece.getColumn();

		this.characteristics = new boolean[5];
		this.setCharacteristics();


	}

	public void setPosition(int row, int column) {
		this.row = row;
		this.column = column;
		this.setInPlay(true);
	}

	public int getPieceID() {
		return this.pieceID;
	}

	public int getRow() {
		return this.row;
	}

	public int getColumn() {
		return this.column;
	}


	public void setInPlay(boolean inPlay) {
		this.inPlay = inPlay;
	}

	public boolean isInPlay() {
		return this.inPlay;
	}

	public void setCharacteristics() {

		String tempBinaryString = (Integer.toBinaryString(this.pieceID));
		//String binaryString = tempBinaryString.substring(tempBinaryString.length() -5);
		String binaryString = String.format("%5s", Integer.toBinaryString(this.pieceID)).replace(' ', '0');
		//System.out.println("binary string: " + binaryString);
		
		for (int i = 0; i < this.characteristics.length; i++){
			char binaryChar = binaryString.charAt(i); 
			if(binaryChar == '1') {
				this.characteristics[i] = true;
			} else {
				this.characteristics[i] = false;
			}
		}
		
	}

	public boolean isTall() {
		return this.characteristics[0];
	}
	public boolean isSolid() {
		return this.characteristics[1];
	}
	public boolean isWhite() {
		return this.characteristics[2];
	}
	public boolean isWood() {
		return this.characteristics[3];
	}
	public boolean isRound() {
		return this.characteristics[4];
	}


	public String getHeight() {
		if(this.isTall()) {
			return "tall";
		} else {
			return "short";
		}
	}
	public String getStructure() {
		if(this.isSolid()) {
			return "solid";
		} else {
			return "hollow";
		}
	}
	public String getColor() {
		if(this.isWhite()) {
			return "white";
		} else {
			return "black";
		}
	}
	public String getMaterial() {
		if(this.isWood()) {
			return "wood";
		} else {
			return "metal";
		}
	}
	public String getShape() {
		if(this.isRound()) {
			return "round";
		} else {
			return "square";
		}
	}

	public boolean[] getCharacteristicsArray() {
		return this.characteristics;
	}
	public String binaryStringRepresentation() {
		return String.format("%5s", Integer.toBinaryString(this.pieceID)).replace(' ', '0');
	}
	


}
