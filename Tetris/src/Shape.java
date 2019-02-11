import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class Shape {
	private BufferedImage blocks;
	private Board board;
	private int[][] coords;
	// �����̴� ��ǥ
	private int deltaX;
	// ���� ��ǥ ó��
	private int x, y;
	// �ӵ�
	private long time, lastTime;
	private int normalSpeed = 400, downSpeed = 60, currentSpeed;
	
	private boolean collision =false, moveX=false;
	private int color;
	

	// �������� �� ��� 2���� �迭��
	public Shape(BufferedImage blocks, Board board, int[][] coords, int color) {
		super();
		this.blocks = blocks;
		this.board = board;
		this.coords = coords;
		this.color = color;
		this.x = 3;
		this.y = 0;
		time = 0;
		lastTime = System.currentTimeMillis();
		currentSpeed =normalSpeed;
	}

	// �� �����̱� (Ű ������), �ð�
	public void update() {
		time += System.currentTimeMillis() - lastTime;
		lastTime = System.currentTimeMillis();
		
		//���� ��������
		if(collision) {
			
			//�� ���̱�
			for(int row=0; row <coords.length; row++) {
				for (int col =0; col<coords[0].length; col++) {
					if(coords[row][col] != 0) {
						board.getBoard()[y+row][x+col] =color;
					}
				}
			}
			board.setNewShape();
			checkLine();
		}
		//y��ǥ ���� �� ��������
		if((y+coords.length+1<20)) {
			//�Ʒ��� ���� �����ϴ��� Ȯ��
			for(int row=0; row <coords.length; row++) {
				for (int col =0; col<coords[0].length; col++) {
					if(coords[row][col] != 0) {
						if(board.getBoard()[y+row+1][x+col] !=0)
							collision =true;
					}
				}
			}
		if (time > currentSpeed) {
			y++;
			time = 0;
		}
		}else {
			collision=true;
		}
		//�翷���� �̵�
		if (x + deltaX + coords[0].length <= 10 && x + deltaX >= 0) {
			for(int row=0; row <coords.length; row++) {
				for (int col =0; col<coords[0].length; col++) {
					if(coords[row][col] != 0) {
						if(board.getBoard()[y+row][x+deltaX+col] !=0)
							moveX= false;
					}
				}
			}
			if(moveX)
			x += deltaX;
		}
		deltaX  = 0;
		moveX=true;
	}

	// �� �׸���
	public void render(Graphics g) {
		for (int row = 0; row < coords.length; row++) {
			for (int column = 0; column < coords[0].length; column++) {
				if (coords[row][column] != 0)
					g.drawImage(blocks, column * board.getBlockSize() + x * board.getBlockSize(),
							row * board.getBlockSize() + y * board.getBlockSize(), null);
			}
		}
	}
	//�پ��ֱ�
	private void checkLine() {
		int height = board.getBoard().length-1;
		for( int i=height; i>0; i--) {
			int count =0;
			for(int j=0; j<board.getBoard()[0].length; j++) {
				if(board.getBoard()[i][j]!=0)
					count++;
				board.getBoard()[height][j]=board.getBoard()[i][j];
			}
			if(count< board.getBoard()[0].length)
				height--;
		}
		
	}

	// �����̴� ��ǥ ����(Ű���� ������)
	public void setDeltaX(int deltaX) {
		this.deltaX = deltaX;
	}


	// �ӵ� ������
	public void speedDown() {
		currentSpeed = downSpeed;
	}
	//���� �ӵ���
	public void normalSpeed() {
		currentSpeed = normalSpeed;
	}
	//�� ����
	public void rotate() {
		int[][] rotatedMatrix =null;
		rotatedMatrix = getTransposeMatrix(coords);
		rotatedMatrix = getReverseMatrix(rotatedMatrix);
		
		if(rotatedMatrix[0].length+x>=10||rotatedMatrix.length+y>=20)
			return;
		//�ֺ��� ��Ȯ��
				for(int row=0; row<rotatedMatrix.length; row++) {
					for(int col =0; col<rotatedMatrix[0].length; col++) {
						if(board.getBoard()[y+row][x+col]!=0)
							return;
					}
				}
		coords =rotatedMatrix;
		
	}
	//�� ������
	private int [][] getTransposeMatrix(int[][] matrix){
		int [][] newMatrix = new int[matrix[0].length][matrix.length];
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[0].length; j++) {
				newMatrix[j][i] = matrix[i][j]; 
			}
		}
		return newMatrix;
	}
	//�� �ݴ�� ������
	private int [][] getReverseMatrix(int[][] matrix){
		int middle = matrix.length/2;
		for (int i =0; i<middle ; i++) {
			int[] m = matrix[i];
			matrix[i] = matrix[matrix.length -i-1];
			matrix[matrix.length-i-1]=m;
		}
		return matrix;
		
	}

	public BufferedImage getBlocks() {
		return blocks;
	}

	public int[][] getCoords() {
		return coords;
	}

	public int getColor() {
		return color;
	}
	

}
