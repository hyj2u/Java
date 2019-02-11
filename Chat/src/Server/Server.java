package Server;

import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Server extends JFrame implements ActionListener {

	// GUI ����
	private JPanel contentPane;
	private JTextArea textArea;
	private JLabel portLabel;
	private TextField textField;
	private JButton startBtn;
	private JButton endBtn;
	// ��Ʈ��ũ ����
	private ServerSocket serverSocket;
	private Socket socket;
	private int port;

	private Vector<UserInfo> userVector = new Vector<>();
	private StringTokenizer st;

	private Vector<RoomInfo> roomVector = new Vector<>();

	public Server() {
		init();
		start();
	}

	// ��ư �׼� ������ �޼���
	private void start() {
		startBtn.addActionListener(this);
		endBtn.addActionListener(this);
	}

	// GUI
	private void init() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(17, 15, 394, 280);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		portLabel = new JLabel("��Ʈ ��ȣ");
		portLabel.setFont(new Font("���� ���", Font.PLAIN, 20));
		portLabel.setBounds(30, 323, 96, 42);
		contentPane.add(portLabel);

		textField = new TextField();
		textField.setBounds(132, 324, 272, 41);
		textField.setFont(new Font("Arial", Font.PLAIN, 20));
		contentPane.add(textField);

		startBtn = new JButton("���� ����");
		startBtn.setBounds(17, 380, 175, 49);
		startBtn.setFont(new Font("���� ���", Font.PLAIN, 20));
		contentPane.add(startBtn);

		endBtn = new JButton("���� ����");
		endBtn.setBounds(236, 380, 175, 49);
		endBtn.setFont(new Font("���� ���", Font.PLAIN, 20));
		endBtn.setEnabled(false);
		contentPane.add(endBtn);

		this.setVisible(true);
	}

	// ��������
	private void Server_start() {
		try {
			serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "�̹� ������� ��Ʈ", "�˸�", JOptionPane.ERROR_MESSAGE);
		} // ��Ʈ��ȣ

		if (serverSocket != null) {
			Connection();
		}

	}

	// ����� ���� �޼���
	private void Connection() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("����� ���� ����� \n");
						socket = serverSocket.accept();// ����� ���� ���
						textArea.append("����� ���� �Ϸ� \n");

						UserInfo user = new UserInfo(socket);
						user.start();

					} catch (IOException e) {

					}
				}
			}
		});
		t.start();
	}

	@Override

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startBtn) {
			port = Integer.parseInt(textField.getText().trim());
			Server_start();// ���� ���� ����� ���� ���
			startBtn.setEnabled(false);
			textField.setEditable(false);
			endBtn.setEnabled(true);
		} else if (e.getSource() == endBtn) {
			startBtn.setEnabled(true);
			textField.setEditable(true);
			endBtn.setEnabled(false);
			try {
				serverSocket.close();
				userVector.removeAllElements();
				roomVector.removeAllElements();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	// ���� ����� ��ü�� ����
	public class UserInfo extends Thread {

		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private Socket userSocket;
		private String nickname;
		private String msg;

		private boolean roomCheck = true;

		public UserInfo(Socket soc) {
			userSocket = soc;
			UserNetwork();
		}

		// ����ں� ��Ʈ��ũ ����
		private void UserNetwork() {
			try {
				is = userSocket.getInputStream();
				os = userSocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

				nickname = dis.readUTF();
				textArea.append(nickname + "�� ����");

				// �����ڵ鿡�� �������� �˸�
				Broadcast("New User/" + nickname);

				// �������ڿ��� ���� ������ �˸�
				for (int i = 0; i < userVector.size(); i++) {
					UserInfo u = userVector.elementAt(i);
					sendMessage("Old User/" + u.nickname);
				}
				// ���� �� �˸�
				for (int i = 0; i < roomVector.size(); i++) {
					sendMessage("Old Room/" + roomVector.elementAt(i).roomName);

				}
				sendMessage("RoomListUpdate/ ");
				userVector.add(this);
				Broadcast("UserListUpdate/ ");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "��Ʈ�� ���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
			}

		}

		// ���� �޽��� ��� �ޱ�
		@Override
		public void run() {
			while (true) {
				try {
					this.msg = dis.readUTF();
					textArea.append(nickname + "����ڷκ��� ���� �޽���: " + msg + "\n");
					inMessage(msg);
				} catch (IOException e) {
					textArea.append(nickname + "�� ���� ������ \n");
					try {
						dos.close();
						dis.close();
						userSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					userVector.remove(this);
					Broadcast("UserOut/" + nickname);
					Broadcast("UserListUpdate/ ");
					break;
				}
			}
		}

		// �޽��� �ޱ�
		private void inMessage(String msg) {
			st = new StringTokenizer(msg, "/");
			String protocol = st.nextToken();
			String message = st.nextToken();
			System.out.println("protocol= " + protocol);

			if (protocol.equals("note")) {

				String note = st.nextToken();
				System.out.println(note);

				// ���Ϳ��� ����� ã�Ƽ� �޽��� ������
				for (int i = 0; i < userVector.size(); i++) {
					if (userVector.elementAt(i).nickname.equals(message)) {
						sendMessage("note/" + nickname + "/" + note);
					}
				}
			} else if (protocol.equals("CreateRoom")) {
				// 1. �ߺ��� ���ִ��� Ȯ��
				for (int i = 0; i < roomVector.size(); i++) {
					if (roomVector.elementAt(i).roomName.equals(message)) {
						sendMessage("RoomCreateFail/ ");
						roomCheck = true;
						break;
					}
				}
				// �ߺ��� ���� ���� ���
				if (roomCheck) {
					RoomInfo newRoom = new RoomInfo(message, this);
					roomVector.add(newRoom);
					sendMessage("CreateRoom/" + message);
					// ������ ���� ���� ��ο���
					Broadcast("NewRoom/" + message);
				}
				roomCheck = true;
			} else if (protocol.equals("Chat")) {
				String chatMsg = st.nextToken();
				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = roomVector.elementAt(i);
					if (r.roomName.equals(message)) {
						r.roomBroadcast("Chat/" + nickname + "/" + chatMsg);
					}
				}
			} else if (protocol.equals("JoinRoom")) {
				for (int i = 0; i < roomVector.size(); i++) {
					RoomInfo r = roomVector.elementAt(i);
					if (r.roomName.equals(message)) {
						r.roomBroadcast("Chat/�˸�/***" + nickname + "���� �����ϼ̽��ϴ�***");
						r.roomUserVector.add(this);
						sendMessage("JoinRoom/" + message);
					}
				}
			}
		}

		// �޽��� ������
		public void sendMessage(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// ��ε� ĳ��Ʈ
		public void Broadcast(String str) {
			for (int i = 0; i < userVector.size(); i++) {
				UserInfo u = userVector.elementAt(i);
				u.sendMessage(str);
			}
		}
	}

	// ä�ù� ��ü�� ����
	public class RoomInfo {
		private String roomName;
		private Vector<UserInfo> roomUserVector = new Vector<>();

		public RoomInfo(String name, UserInfo u) {
			this.roomName = name;
			roomUserVector.add(u);
		}

		public void roomBroadcast(String str) {
			for (int j = 0; j < roomUserVector.size(); j++) {
				UserInfo u = roomUserVector.elementAt(j);
				u.sendMessage(str);
			}
		}

		// ������ �濡 ����� �߰�
		private void UserAdd(UserInfo u) {
			roomUserVector.add(u);
		}
	}
}
