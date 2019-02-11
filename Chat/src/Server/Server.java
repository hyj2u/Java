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

	// GUI 변수
	private JPanel contentPane;
	private JTextArea textArea;
	private JLabel portLabel;
	private TextField textField;
	private JButton startBtn;
	private JButton endBtn;
	// 네트워크 변수
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

	// 버튼 액션 리스너 메서드
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

		portLabel = new JLabel("포트 번호");
		portLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		portLabel.setBounds(30, 323, 96, 42);
		contentPane.add(portLabel);

		textField = new TextField();
		textField.setBounds(132, 324, 272, 41);
		textField.setFont(new Font("Arial", Font.PLAIN, 20));
		contentPane.add(textField);

		startBtn = new JButton("서버 실행");
		startBtn.setBounds(17, 380, 175, 49);
		startBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		contentPane.add(startBtn);

		endBtn = new JButton("서버 중지");
		endBtn.setBounds(236, 380, 175, 49);
		endBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 20));
		endBtn.setEnabled(false);
		contentPane.add(endBtn);

		this.setVisible(true);
	}

	// 서버소켓
	private void Server_start() {
		try {
			serverSocket = new ServerSocket(port);

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트", "알림", JOptionPane.ERROR_MESSAGE);
		} // 포트번호

		if (serverSocket != null) {
			Connection();
		}

	}

	// 사용자 연결 메서드
	private void Connection() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						textArea.append("사용자 접속 대기중 \n");
						socket = serverSocket.accept();// 사용자 접속 대기
						textArea.append("사용자 접속 완료 \n");

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
			Server_start();// 소켓 생성 사용자 접속 대기
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

	// 접속 사용자 객체로 저장
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

		// 사용자별 네트워크 설정
		private void UserNetwork() {
			try {
				is = userSocket.getInputStream();
				os = userSocket.getOutputStream();
				dis = new DataInputStream(is);
				dos = new DataOutputStream(os);

				nickname = dis.readUTF();
				textArea.append(nickname + "이 접속");

				// 접속자들에게 새접속자 알림
				Broadcast("New User/" + nickname);

				// 새접속자에게 기존 접속자 알림
				for (int i = 0; i < userVector.size(); i++) {
					UserInfo u = userVector.elementAt(i);
					sendMessage("Old User/" + u.nickname);
				}
				// 기존 방 알림
				for (int i = 0; i < roomVector.size(); i++) {
					sendMessage("Old Room/" + roomVector.elementAt(i).roomName);

				}
				sendMessage("RoomListUpdate/ ");
				userVector.add(this);
				Broadcast("UserListUpdate/ ");
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "스트림 설정 오류", "알림", JOptionPane.ERROR_MESSAGE);
			}

		}

		// 소켓 메시지 계속 받기
		@Override
		public void run() {
			while (true) {
				try {
					this.msg = dis.readUTF();
					textArea.append(nickname + "사용자로부터 들어온 메시지: " + msg + "\n");
					inMessage(msg);
				} catch (IOException e) {
					textArea.append(nickname + "님 접속 끊어짐 \n");
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

		// 메시지 받기
		private void inMessage(String msg) {
			st = new StringTokenizer(msg, "/");
			String protocol = st.nextToken();
			String message = st.nextToken();
			System.out.println("protocol= " + protocol);

			if (protocol.equals("note")) {

				String note = st.nextToken();
				System.out.println(note);

				// 벡터에서 사용자 찾아서 메시지 보내기
				for (int i = 0; i < userVector.size(); i++) {
					if (userVector.elementAt(i).nickname.equals(message)) {
						sendMessage("note/" + nickname + "/" + note);
					}
				}
			} else if (protocol.equals("CreateRoom")) {
				// 1. 중복된 방있는지 확인
				for (int i = 0; i < roomVector.size(); i++) {
					if (roomVector.elementAt(i).roomName.equals(message)) {
						sendMessage("RoomCreateFail/ ");
						roomCheck = true;
						break;
					}
				}
				// 중복된 방이 없을 경우
				if (roomCheck) {
					RoomInfo newRoom = new RoomInfo(message, this);
					roomVector.add(newRoom);
					sendMessage("CreateRoom/" + message);
					// 새방이 생긴 것을 모두에게
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
						r.roomBroadcast("Chat/알림/***" + nickname + "님이 입장하셨습니다***");
						r.roomUserVector.add(this);
						sendMessage("JoinRoom/" + message);
					}
				}
			}
		}

		// 메시지 보내기
		public void sendMessage(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 브로드 캐스트
		public void Broadcast(String str) {
			for (int i = 0; i < userVector.size(); i++) {
				UserInfo u = userVector.elementAt(i);
				u.sendMessage(str);
			}
		}
	}

	// 채팅방 객체로 저장
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

		// 입장한 방에 사용자 추가
		private void UserAdd(UserInfo u) {
			roomUserVector.add(u);
		}
	}
}
