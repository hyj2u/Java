package Client;

import java.awt.Button;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class Client extends JFrame implements ActionListener, KeyListener {
	// �α��� GUI ����
	private JFrame login_frame = new JFrame();
	private JPanel loginPane;
	private TextField ipText;
	private TextField portText;
	private TextField idText;
	private JButton connectBtn;

	// ä��â GUI ����
	private JPanel contentPane;
	private JList<String> peopleList;
	private JButton sendNoteBtn;
	private JList<String> chatList;
	private JButton roomInBtn;
	private JButton roomMakeBtn;
	private TextField msgText;
	private TextArea textArea;
	private Button sendMsgBtn;

	// ��Ʈ��ũ ����
	private String ip;
	private int port;
	private String id;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private Vector<String> userVlist = new Vector<>();
	private Vector<String> roomVlist = new Vector<>();
	StringTokenizer st;
	private String myRoom; // ���� �ִ� ä�ù�

	public Client() {
		Login_init();
		Chat_init();
		start();
	}

	// ��ư �׼Ǹ����� -����
	private void start() {
		connectBtn.addActionListener(this);
		sendNoteBtn.addActionListener(this);
		roomInBtn.addActionListener(this);
		roomMakeBtn.addActionListener(this);
		sendMsgBtn.addActionListener(this);
		msgText.addKeyListener(this);
	}

	// ��Ʈ��ũ ����
	private void Network() {
		try {
			socket = new Socket(ip, port);
			Connection();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		}

	}

	// �������� ���� �޼��� connection
	private void Connection() {
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);

			// id �� ������ ���
			sendMessage(id);
			userVlist.add(id);

		} catch (IOException e) {

			JOptionPane.showMessageDialog(null, "���� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		}
		this.setVisible(true);
		this.login_frame.setVisible(false);
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						String msg = dis.readUTF();
						System.out.println("server: " + msg);
						inMessage(msg);

					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "���� ������", "�˸�", JOptionPane.ERROR_MESSAGE);

						try {
							is.close();
							os.close();
							dis.close();
							dos.close();

						} catch (IOException e1) {

						}
						break;
					}
				}

			}
		});
		t.start();
	}

	// �޽��� �ޱ�
	private void inMessage(String str) {
		st = new StringTokenizer(str, "/");
		String protocol = st.nextToken();
		String msg = st.nextToken();
		System.out.println(protocol);
		System.out.println(msg);

		if (protocol.equals("New User")) {
			userVlist.add(msg);

		} else if (protocol.equals("Old User")) {
			userVlist.add(msg);

		} else if (protocol.equals("note")) {
			String note = st.nextToken();

			System.out.println(note);

			JOptionPane.showMessageDialog(null, note, msg + "���� ����", JOptionPane.CLOSED_OPTION);
		} else if (protocol.equals("UserListUpdate")) {
			peopleList.setListData(userVlist);
			// �游��� ���� ���� ��
		} else if (protocol.equals("CreateRoom")) {
			myRoom = msg;
			sendMsgBtn.setEnabled(true);
			msgText.setEnabled(true);
			roomInBtn.setEnabled(false);
			roomMakeBtn.setEnabled(false);
			// �游��� �������� ��
		} else if (protocol.equals("RoomCreateFail")) {
			JOptionPane.showMessageDialog(null, "�游��� ����", "�˸�", JOptionPane.ERROR_MESSAGE);
		} else if (protocol.equals("NewRoom")) {// ������ ������ ��
			roomVlist.add(msg);
			chatList.setListData(roomVlist);
		} else if (protocol.equals("Chat")) {// ä��
			String text = st.nextToken();
			textArea.append(msg + ": " + text + "\n");
			// ä�ù� ��� ������Ʈ
		} else if (protocol.equals("RoomListUpdate")) {
			chatList.setListData(roomVlist);
		} else if (protocol.equals("Old Room")) {
			roomVlist.add(msg);
			// �濡 ���� �������� ��
		} else if (protocol.equals("JoinRoom")) {
			myRoom = msg;
			sendMsgBtn.setEnabled(true);
			msgText.setEnabled(true);
			roomInBtn.setEnabled(false);
			roomMakeBtn.setEnabled(false);
			JOptionPane.showMessageDialog(null, "ä�ù濡 �����߽��ϴ�", "�˸�", JOptionPane.INFORMATION_MESSAGE);
		} else if (protocol.equals("UserOut")) {
			userVlist.remove(msg);
		}
	}

	// �޽��� ������
	private void sendMessage(String str) {
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �α��� GUI
	private void Login_init() {
		login_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		login_frame.setBounds(100, 100, 450, 500);
		loginPane = new JPanel();
		loginPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		login_frame.setContentPane(loginPane);
		loginPane.setLayout(null);

		JLabel lblServerIp = new JLabel("Server Port");
		lblServerIp.setFont(new Font("Arial", Font.PLAIN, 20));
		lblServerIp.setBounds(17, 262, 129, 42);
		loginPane.add(lblServerIp);

		JLabel label = new JLabel("Server IP");
		label.setFont(new Font("Arial", Font.PLAIN, 20));
		label.setBounds(17, 186, 96, 42);
		loginPane.add(label);

		JLabel lblId = new JLabel("ID");
		lblId.setFont(new Font("Arial", Font.PLAIN, 20));
		lblId.setBounds(17, 331, 96, 42);
		loginPane.add(lblId);

		ipText = new TextField();
		ipText.setBounds(157, 186, 249, 41);
		ipText.setFont(new Font("Arial", Font.PLAIN, 20));
		loginPane.add(ipText);

		portText = new TextField();
		portText.setBounds(157, 262, 249, 41);
		portText.setFont(new Font("Arial", Font.PLAIN, 20));
		loginPane.add(portText);

		idText = new TextField();
		idText.setFont(new Font("Arial", Font.PLAIN, 20));
		idText.setBounds(157, 332, 249, 41);
		loginPane.add(idText);

		connectBtn = new JButton("�� ��");
		connectBtn.setBounds(59, 387, 306, 42);
		loginPane.add(connectBtn);

		login_frame.setVisible(true);
	}

	// Chat GUI
	private void Chat_init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 824, 749);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel label = new JLabel("�� ü �� �� ��");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("���� ���", Font.PLAIN, 18));
		label.setBounds(17, 15, 163, 42);
		contentPane.add(label);

		peopleList = new JList<String>();
		peopleList.setBounds(17, 61, 163, 207);
		contentPane.add(peopleList);
		peopleList.setListData(userVlist);
		peopleList.setFont(new Font("���� ���", Font.PLAIN, 18));

		sendNoteBtn = new JButton("���� ������");
		sendNoteBtn.setBounds(17, 283, 163, 29);
		contentPane.add(sendNoteBtn);

		JLabel label_1 = new JLabel("ä �� �� �� ��");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("���� ���", Font.PLAIN, 18));
		label_1.setBounds(17, 327, 163, 42);
		contentPane.add(label_1);

		chatList = new JList<String>();
		chatList.setBounds(17, 373, 163, 207);
		contentPane.add(chatList);
		chatList.setListData(roomVlist);
		chatList.setFont(new Font("���� ���", Font.PLAIN, 18));

		roomInBtn = new JButton("ä�ù� ����");
		roomInBtn.setBounds(17, 595, 163, 29);
		contentPane.add(roomInBtn);

		roomMakeBtn = new JButton("�� �����");
		roomMakeBtn.setBounds(17, 629, 163, 29);
		contentPane.add(roomMakeBtn);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(201, 61, 591, 519);
		contentPane.add(scrollPane);

		textArea = new TextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		textArea.setFont(new Font("���� ���", Font.PLAIN, 14));

		msgText = new TextField();
		msgText.setBounds(204, 605, 504, 42);
		msgText.setFont(new Font("���� ���", Font.PLAIN, 16));
		msgText.setEnabled(false);
		contentPane.add(msgText);

		sendMsgBtn = new Button("�� ��");
		sendMsgBtn.setBounds(726, 605, 68, 42);
		sendMsgBtn.setEnabled(false);
		contentPane.add(sendMsgBtn);

		setVisible(false);
	}

	// �׼Ǹ�����
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connectBtn) {
			if (ipText.getText().length() == 0) {
				ipText.setText("IP �ּҸ� �Է��ϼ���");
				ipText.requestFocus();
			} else if (portText.getText().length() == 0) {
				portText.setText("��Ʈ ��ȣ�� �Է��ϼ���");
				portText.requestFocus();
			} else if (idText.getText().length() == 0) {
				idText.setText("ID�� �Է��ϼ���");
				idText.requestFocus();
			} else {
				ip = ipText.getText().trim();
				port = Integer.parseInt(portText.getText().trim());
				id = idText.getText().trim();
				Network();
			}
		}
		// ����������
		if (e.getSource() == sendNoteBtn) {
			String user = peopleList.getSelectedValue();
			String note = JOptionPane.showInputDialog("���� �޽���");
			if (note != null) {
				sendMessage("note/" + user + "/" + note);
				System.out.println("���� �޽��� : " + note + "���� �����:" + user);
			}
		}
		// �������ϱ� ��ư
		if (e.getSource() == roomInBtn) {
			String joinRoom = chatList.getSelectedValue();
			sendMessage("JoinRoom/" + joinRoom);
		}
		// �游��� ��ư
		if (e.getSource() == roomMakeBtn) {
			String roomName = JOptionPane.showInputDialog("�� �̸�");
			if (roomName != null) {
				sendMessage("CreateRoom/" + roomName);
			}
		}
		// �޽��� ���� (ä��â)
		if (e.getSource() == sendMsgBtn) {
			sendMessage("Chat/" + myRoom + "/" + msgText.getText().trim());
			msgText.setText("");
			msgText.requestFocus();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			sendMessage("Chat/" + myRoom + "/" + msgText.getText().trim());
			msgText.setText("");
			msgText.requestFocus();
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
