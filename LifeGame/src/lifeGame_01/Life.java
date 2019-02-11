package lifeGame_01;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Life implements MouseListener, ActionListener, Runnable {

	boolean[][] cells = new boolean[10][10];
	int[][] neighbors = new int[10][10];
	boolean gameStart =false;
	JFrame frame = new JFrame("Conway's Game of Life");
	LifePanel panel = new LifePanel(cells);
	Container south = new Container();
	JButton step = new JButton("STEP");
	JButton start = new JButton("START");
	JButton stop = new JButton("STOP");
	
	public Life() {
		// GUI
		// frame, panel
		frame.setSize(600, 600);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		panel.addMouseListener(this);

		// container south, button
		south.setLayout(new GridLayout(1, 3));
		south.add(start);
		south.add(step);
		south.add(stop);
		start.addActionListener(this);
		stop.addActionListener(this);
		step.addActionListener(this);
		frame.add(south, BorderLayout.SOUTH);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	// 마우스 클릭으로 칸 색변경
	@Override
	public void mouseReleased(MouseEvent e) {

		double width = (double) panel.getWidth() / cells[0].length;
		double height = (double) panel.getHeight() / cells.length;
		int x = Math.min(cells[0].length - 1, (int) (e.getX() / width));
		int y = Math.min(cells.length - 1, (int) (e.getY() / height));
		System.out.println(x + ", " + y);
		cells[x][y] = !cells[x][y];
		panel.repaint();

	}

	// 버튼기능
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == step) {
			step();

		}else if(e.getSource() ==start) {
			stop.setEnabled(true);
			start.setEnabled(false);
			gameStart=true;
			Thread t = new Thread(this);
			
			t.start();
		}else {
			stop.setEnabled(false);
			start.setEnabled(true);
			gameStart=false;
			clear();
			
		}

	}

	// 주위 체크
	public int[][] countLife(int cellX, int cellY) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
			 
					if(!(x==0&&y==0)) {
					if (cellX + x >= 0 && cellX + x < cells[0].length && cellY + y >=0 && cellY + y < cells.length) {
						if (cells[cellX + x][cellY + y]) {
							neighbors[cellX][cellY]++;
						}
					
				}
			}
			}
		}
		return neighbors;
	}

	// 실행 :step
	public void step() {
		
		for(int r=0; r<neighbors.length; r++) {
			for (int j=0; j<neighbors[0].length; j++) {
				neighbors[r][j]=0;
			}
		}
		
		for (int x = 0; x < cells[0].length; x++) {
			for (int y = 0; y < cells.length; y++) {
				countLife(x, y);
			}
		}
		/*for(int r=0; r<neighbors.length; r++) {
			System.out.println(Arrays.toString(neighbors[r]));
		}*/
		for (int x = 0; x < cells[0].length; x++) {
			for (int y = 0; y < cells.length; y++) {
				if (cells[x][y]) {
					switch (neighbors[x][y]) {
					case 2:
					case 3:
						cells[x][y] = true;
						break;
					default:
						cells[x][y] = false;
					}
				} else {
					switch (neighbors[x][y]) {
					case 3:
						cells[x][y] = true;
						break;
					default:
						cells[x][y] = false;
					}
				}
			}
		}
		
		
		panel.repaint();
	}
	
	public void clear() {
		for(int r=0; r<cells.length; r++) {
			for (int j=0; j<cells[0].length; j++) {
				cells[r][j]=false;
			}
		}
		panel.repaint();
	}

	@Override
	public void run() {
		while(gameStart) {
			step();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	}

}
