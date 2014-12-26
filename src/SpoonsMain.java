import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class SpoonsMain {
	private static final int DEFAULT_PORT = 1235;

	public static void main(String[] args) {
		
		String playerName;

		JLabel message = new JLabel("");

		message = new JLabel("Welcome to Spoons!", JLabel.CENTER);

		message.setFont(new Font("Serif", Font.BOLD, 16));

		final JComboBox numPlayersSelector = new JComboBox();
		final JTextField listeningPortInput = new JTextField("" + DEFAULT_PORT,
				5);
		final JTextField hostInput = new JTextField(15);
		final JTextField connectPortInput = new JTextField("" + DEFAULT_PORT, 5);
		final JTextField nameInput = new JTextField(31);

		final JRadioButton selectServerMode = new JRadioButton(
				"Start a new game");
		final JRadioButton selectClientMode = new JRadioButton(
				"Connect to existing game");

		ButtonGroup group = new ButtonGroup();
		group.add(selectServerMode);
		group.add(selectClientMode);
		ActionListener radioListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == selectServerMode) {
					listeningPortInput.setEnabled(true);
					numPlayersSelector.setEnabled(true);
					hostInput.setEnabled(false);
					connectPortInput.setEnabled(false);
					nameInput.setEnabled(false);
					
					listeningPortInput.setEditable(true);
					numPlayersSelector.setEnabled(true);
					hostInput.setEditable(false);
					connectPortInput.setEditable(false);
					nameInput.setEditable(false);
					
				} else {
					listeningPortInput.setEnabled(false);
					numPlayersSelector.setEnabled(false);
					hostInput.setEnabled(true);
					connectPortInput.setEnabled(true);
					nameInput.setEnabled(true);
					
					listeningPortInput.setEditable(false);
					numPlayersSelector.setEnabled(false);
					hostInput.setEditable(true);
					connectPortInput.setEditable(true);
					nameInput.setEditable(true);
					
				}
			}
		};
		selectServerMode.addActionListener(radioListener);
		selectClientMode.addActionListener(radioListener);
		selectServerMode.setSelected(true);
		hostInput.setEnabled(false);
		connectPortInput.setEnabled(false);
		nameInput.setEnabled(false);
		
		hostInput.setEditable(false);
		connectPortInput.setEditable(false);
		nameInput.setEditable(false);
		

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0, 1, 5, 5));
		inputPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.BLACK, 2),
				BorderFactory.createEmptyBorder(6, 6, 6, 6)));

		inputPanel.add(message);

		JPanel row;
		JPanel row2;

		inputPanel.add(selectServerMode);

		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Listen on port: "));
		row.add(listeningPortInput);
		inputPanel.add(row);

		numPlayersSelector.addItem("4");
		numPlayersSelector.addItem("5");
		numPlayersSelector.addItem("6");
		numPlayersSelector.setSelectedIndex(2);

		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Number of players: "));
		row.add(numPlayersSelector);

		row2 = new JPanel();
		String ipAddress = "";
		try {
			ipAddress = "" + InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		ipAddress = ipAddress.replaceAll("[^\\d.]", "");
		while (ipAddress.charAt(0) == '.'){
			ipAddress = ipAddress.substring(1);
		}
		
		row2.add(new JLabel("Your IP Address is: " + ipAddress));
		inputPanel.add(row2);

		inputPanel.add(selectClientMode);

		row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Computer: "));
		row.add(hostInput);
		row.add(Box.createHorizontalStrut(19));
		row.add(new JLabel("Port Number: "));
		row.add(connectPortInput);
		
		inputPanel.add(row);

		row = new JPanel();
		row.add(Box.createHorizontalStrut(40));
		row.add(new JLabel("Your Name"));
		row.add(nameInput);
		inputPanel.add(row);

		// Show the dialog, get the user's response and -- if the user doesn't
		// cancel -- start a game. If the user chooses to run as the server
		// then a PokerHub (server) is created and after that a PokerWindow
		// is created that connects to the server running on localhost, which
		// was
		// just created. In that case, the game will wait for a second
		// connection.
		// If the user chooses to connect to an existing server, then only
		// a PokerWindow is created, that will connect to the specified
		// host where the server is running.

		while (true) { // Repeats until a game is started or the user cancels.

			int action = JOptionPane.showConfirmDialog(null, inputPanel,
					"Spoons", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (action != JOptionPane.OK_OPTION)
				return;

			if (selectServerMode.isSelected()) {
				int port;
				try {
					port = Integer
							.parseInt(listeningPortInput.getText().trim());
					if (port <= 0)
						throw new Exception();
				} catch (Exception e) {
					message.setText("Illegal port number!");
					listeningPortInput.selectAll();
					listeningPortInput.requestFocus();
					continue;
				}
				try {
					new SpoonsHub(port, numPlayersSelector.getSelectedIndex() + 4);
				} catch (Exception e) {
					message.setText("Error: Can't listen on port " + port);
					e.printStackTrace();
					listeningPortInput.selectAll();
					listeningPortInput.requestFocus();
					continue;
				}

				break;
			} else {
				String host;
				int port;
				host = hostInput.getText().trim();
				if (host.length() == 0) {
					message.setText("You must enter a computer name!");
					hostInput.requestFocus();
					continue;
				}
				if (nameInput.getText().trim().length() <= 0){
					message.setText("You must enter your name.");
					nameInput.requestFocus();
					continue;
				}
				try {
					port = Integer.parseInt(connectPortInput.getText().trim());
					if (port <= 0)
						throw new Exception();
				} catch (Exception e) {
					message.setText("Illegal port number!");
					connectPortInput.selectAll();
					connectPortInput.requestFocus();
					continue;
				}
				new SpoonsWindow(host, port, nameInput.getText().trim());
				break;

			}

		}

	}
}
