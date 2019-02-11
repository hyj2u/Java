import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements KeyListener{

	private BufferedImage blocks;
	// ������
	private final int blockSize = 30;
	private final int boardHeight = 20, boardWidth = 10;
	private int [][] board = new int[boardHeight][boardWidth];
	
	//�� ��� �迭
	private Shape[] shapes = new Shape[7];
	private Shape currentShape;
	//Ÿ�̸�
	private Timer timer;
	private final int FPS =60;
	private final int delay =1000/60;
	
	private boolean gameover = false;


	public Board() {
		try {
			blocks = ImageIO.read(Board.class.getResource("/title.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Timer
		timer = new Timer(delay, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				update();
				repaint();
				
			}
		});
		timer.start();
		//shape �� ��� ����
		shapes[0] = new Shape(blocks.getSubimage(0, 0, blockSize, blockSize), this, 
				new int[][] {{1,1,1,1}},1); //�� ���
		shapes[1] = new Shape(blocks.getSubimage(blockSize, 0, blockSize, blockSize), this, 
				new int[][] {{1,1,0}
							,{0,1,1}},2); //Z���
		shapes[2] = new Shape(blocks.getSubimage(blockSize*2, 0, blockSize, blockSize), this, 
				new int[][] {{0,1,1}
							,{1,1,0}},3); //�ݴ� Z(S) ���
		shapes[3] = new Shape(blocks.getSubimage(blockSize*3, 0, blockSize, blockSize), this, 
				new int[][] {{1,1,1}
							,{0,0,1}},4); //�����
		shapes[4] = new Shape(blocks.getSubimage(blockSize*4, 0, blockSize, blockSize), this, 
				new int[][] {{1,1,1}
							,{1,0,0}},5); //�ݴ� �� ���
		shapes[5] = new Shape(blocks.getSubimage(blockSize*5, 0, blockSize, blockSize), this, 
				new int[][] {{1,1,1}
							,{0,1,0}},6); //�̸��
		shapes[6] = new Shape(blocks.getSubimage(blockSize*6, 0, blockSize, blockSize), this, 
				new int[][] {{1,1}
							,{1,1}},7); //�� ���
		setNewShape();
	}
	public void setNewShape() {
		int index = (int)(Math.random()*shapes.length);
		Shape newShape= new Shape( shapes[index].getBlocks(), this, shapes[index].getCoords(),shapes[index].getColor());
		currentShape = newShape;
		
		//���� ��� ������ Ȯ�� �� ���ӿ���
		for(int row=0; row <currentShape.getCoords().length; row++) {
			for (int col =0; col<currentShape.getCoords()[0].length; col++) {
				if(currentShape.getCoords()[row][col] != 0) {
					if(board[row][col+3] !=0)
						gameover=true;
				}
			}
		}
	}

	// paint
	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		currentShape.render(g);
		//�����̴°� �׸���
		for(int row=0; row <board.length; row++) {
			for (int col =0; col<board[0].length; col++) {
				if(board[row][col] != 0) {
					g.drawImage(blocks.getSubimage((board[row][col]-1)*blockSize, 0, blockSize, blockSize), col*blockSize,
							row * blockSize	, null);
				}
			}
		}
		//���׸���
		for (int i=0; i<boardWidth; i++) {
			g.drawLine(i*blockSize	, 0,i*blockSize, boardHeight*blockSize);
		}
		for (int i=0; i<boardHeight; i++) {
			g.drawLine(0, i*blockSize,boardWidth*blockSize	,i*blockSize);
		}
	}
	public int getBlockSize() {
		return blockSize;
	}
	
	public int getBoardHeight() {
		return boardHeight;
	}

	//update
	public void update() {
		if(gameover)
			timer.stop();
		currentShape.update();
		
	}

	//Ű������ 
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
			currentShape.setDeltaX(-1);
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			currentShape.setDeltaX(1);
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
			currentShape.speedDown();
		if(e.getKeyCode()==KeyEvent.VK_UP)
			currentShape.rotate();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
			currentShape.normalSpeed();	
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public int[][] getBoard() {
		return board;
	}
	
}
