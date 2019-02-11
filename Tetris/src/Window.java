import javax.swing.JFrame;

public class Window {
	public static final int HEIGHT =608, WIDTH=305;
	private JFrame window;
	private Board board;
	
	public Window() {
		//프레임
		window = new JFrame("Tetris Game");
		window.setSize(WIDTH, HEIGHT);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		//board =Jpanel
		board = new Board();
		window.add(board);
		
		//키리스너 추가
		window.addKeyListener(board);
		
		window.setVisible(true);
		
		
	}
}
