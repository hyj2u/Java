package lifeGame_01;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class LifePanel extends JPanel{

	private boolean[][] cells;
	double width;
	double height;
	public LifePanel(boolean[][] cells) {
		this.cells = cells;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	
		width = (double)getWidth() /cells[0].length;
		height = (double)getHeight()/ cells.length;
		//ÇÑÄ­ µå·ÎÀ×
		g.setColor(Color.BLUE);
		for(int x=0; x<cells[0].length; x++) {
			for (int y=0; y<cells.length; y++) {
				if(cells[x][y]) {
					g.fillRect((int)Math.round(x*width),(int)Math.round(y*height),(int) width+1,(int) height+1);
				}
			}
		}
		//°ÝÀÚ ÆÐ³Î
		g.setColor(Color.BLACK);
		for(int x=0; x<cells[0].length+1; x++) {
			g.drawLine((int)(x*width), 0,(int)( x*width), getHeight());
		}
		for(int y=0; y<cells.length+1;y++) {
			g.drawLine(0, (int)(y*height),getWidth(), (int)(y*height));
		}
	}
	
}
