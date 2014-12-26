import netgame.common.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SpoonsWindow extends JFrame {

	/**
	 * The constructor sets up the window and makes it visible on the screen. It
	 * starts a thread that will open a connection to a PokerHub. The window
	 * will become operational when the game stops, or it will be closed and the
	 * program terminated if the connection attempt fails.
	 * 
	 * @param hubHostName
	 *            the host name or IP address where the PokerHub is listening.
	 * @param hubPort
	 *            the port number where the PokerHub is listening.
	 */
	public SpoonsWindow(final String hubHostName, final int hubPort, String playerName) {
		super("Spoons");
		try {
			cardImages = new ImageIcon(this.getClass().getResource("/images/cards.png")).getImage();
			//cardImages = ImageIO.read(new File("cards.png"));
			//spoonImage = ImageIO.read(new File("Fruit-Spoon-Front_1024x1024.png"));
			spoonImage = new ImageIcon(this.getClass().getResource("/images/Fruit-Spoon-Front_1024x1024.png")).getImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		display = new Display();
		hand = new ArrayList<SpoonsCard>();
		this.playerName = playerName;

		timer = new Timer(100, new ActionListener() {
			double timeLeft;
			DecimalFormat df = new DecimalFormat("#.#");

			@Override
			public void actionPerformed(ActionEvent e) {
				timerCount++;
				if (timerCount % 20 == 0) {
					if (selectedCard == null) {
						indexOfSelectedCard = (int) (Math.random() * 4);
						selectedCard = hand.get(indexOfSelectedCard);
					}
					//hand.remove(indexOfSelectedCard);
					//hand.add(indexOfSelectedCard, null);
					connection.send(selectedCard);
					
					selectedCard = null;
					timerCount = 0;
				}
				if (timerCount == 10) {
					timerString.setText("1.0");
				} else {
					timeLeft = 2 - (timerCount / 10.0);
					timerString.setText("" + df.format(timeLeft));
				}
				repaint();
			}

		});

		setContentPane(display);
		pack();
		setResizable(false);
		setLocation(200, 100);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() { // A listener to end the program
												// when the user closes the
												// window.
			public void windowClosing(WindowEvent evt) {
				doQuit();
			}
		});
		display.addMouseListener(new MouseAdapter() { // Respond to clicks on
														// the display by
														// calling the doClick()
														// method.
			public void mousePressed(MouseEvent evt) {
				doClick(evt.getX(), evt.getY());
			}
		});
		setVisible(true);
		new Thread() { // A thread to open the connection to the server.
			public void run() {
				try {
					final SpoonsClient c = new SpoonsClient(hubHostName,
							hubPort);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							connection = c;
						}
					});
				} catch (final IOException e) {
					// Error while trying to connect to the server. Tell the
					// user, and end the program. Use
					// SwingUtilties.invokeLater()
					// because this happens in a thread other than the GUI event
					// thread.
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dispose();
							JOptionPane.showMessageDialog(null,
									"Could not connect to " + hubHostName
											+ ".\nError:  " + e);
							System.exit(0);
						}
					});
				}
			}
		}.start();
		
	}

	// ---------------------- Private inner classes
	// -----------------------------------

	/**
	 * A SpoonsClient is a netgame client that handles communication with the
	 * PokerHub. It is used by the SpoonsWindow class to send messages to the
	 * hub. When messages are received from the hub, it takes an appropriate
	 * action.
	 */
	private class SpoonsClient extends Client {

		/**
		 * Connect to a PokerHub at a specified hostname and port number.
		 */
		public SpoonsClient(String hubHostName, int hubPort) throws IOException {
			super(hubHostName, hubPort);
		}

		/**
		 * This method is called when a message from the hub is received by this
		 * client. If the message is of type SpoonsState, then the newState()
		 * method in the SpoonsWindow class is called to handle the change in
		 * the state of the game. If the message is of type String, it
		 * represents a message that is to be displayed to the user; the string
		 * is displayed in the JLabel messageFromServer. If the message is of
		 * type PokerCard[], then it is the opponent's hand. This had is sent
		 * when the game has ended and the player gets to see the opponent's
		 * hand.
		 * <p>
		 * Note that this method is called from a separate thread, not from the
		 * GUI event thread. In order to avoid synchronization issues, this
		 * method uses SwingUtilties.invokeLater() to carry out its task in the
		 * GUI event thread.
		 */
		protected void messageReceived(final Object message) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (message instanceof SpoonsState)
						newState((SpoonsState) message);
					else if (message instanceof String) {
						if (((String) message).equals("start game")) {
							connection.send(playerName);
							display.remove(instructionsLabel);
							timer.start();
							timerLabel.setText("Time Remaining");
							numSpoonsWonLabel.setText("Spoons Won");
							grabbedSpoon = false;
						} else if (((String) message).equals("end game")){
							timer.stop();
							if (!messageFromServer.getText().equals("Congratulations! You got a spoon."))
								messageFromServer.setText("You did not win a spoon.");
							canGrabSpoon = false;
							for (int i = 0; i < 4; i++){
								hand.remove(i);
								hand.add(i, null);
							}
						}else
							messageFromServer.setText((String) message);
					} else if (message instanceof SpoonsCard) {
						hand.set(indexOfSelectedCard, (SpoonsCard) message);
						//System.out.println("Card recieved");

					}
					repaint();
				}
			});
		}

		/**
		 * This method is called when the hub shuts down. That is a signal that
		 * the oppsing player has quit the game. The user is informed of this,
		 * and the program is terminated.
		 */
		protected void serverShutdown(String message) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(SpoonsWindow.this,
							"Too many other players have quit or someone has won." +
							"The game is now over.");
					System.exit(0);
				}
			});
		}

	} // end nested class SpoonsClient

	/**
	 * The display class defines a JPanel that is used as the content pane for
	 * the SpoonsWindow.
	 */
	private class Display extends JPanel {

		/**
		 * The constructor creates labels, buttons, and a text field and adds
		 * them to the panel. An action listener of type ButtonHandler is
		 * created and is added to all the buttons and the text field.
		 */
		Display() {
			setLayout(null); // Layout will be done by hand.
			setPreferredSize(new Dimension(700, 600));
			setBackground(new Color(180, 222, 240));
			setBorder(BorderFactory.createLineBorder(Color.blue, 4));
			instructionsLabel = makeLabel(30, 30, 600, 400, 18, new Color(63, 17, 250));
			instructionsLabel.setText("<html>The goal of the game is to win 3 spoons. You can win a spoon two ways:<br/><br/>" +
					"&nbsp&nbsp&nbsp&nbsp	1. Get 4 of your cards to be the same value (4 Kings, 4 Eights, etc...) <br/><br/> " +
					"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp a. You will have two seconds to determine which ONE of your cards <br/> " +
					"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp you want to discard. <br/><br/>" +
					"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp b. Once you have 4 matching cards, click on a spoon. <br/><br/>" +
					"&nbsp&nbsp&nbsp&nbsp 2. If you see a spoon missing (meaning someone else has 4 of the same " +
					"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp card) then click on a spoon.");
			messageFromServer = makeLabel(100, 500, 600, 100, 30, Color.black);
			timerLabel = makeLabel(500, 170, 200, 20, 20, Color.black);
			timerString = makeLabel(550, 190, 200, 100, 40, Color.red);
			numSpoonsWonLabel = makeLabel(500, 20, 200, 20, 20, Color.black);
			
			

		}

		/**
		 * Utility routine used by constructor to make a label and add it to the
		 * panel. The label has specified bounds, font size, and color, and its
		 * text is initially empty.
		 */
		JLabel makeLabel(int x, int y, int width, int height, int fontSize,
				Color color) {
			JLabel label = new JLabel();
			add(label);
			label.setBounds(x, y, width, height);
			label.setOpaque(false);
			label.setForeground(color);
			label.setFont(new Font("Serif", Font.BOLD, fontSize));
			return label;
		}

		/**
		 * The paint component just draws the cards, when appropriate. The
		 * remaining content of the panel consists of sub-components (labels,
		 * buttons, text field).
		 */
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			for (int i = 1; i <= numSpoonsLeft; i++) {
				g.drawImage(spoonImage, i * 90, 10, 150, 200, this);
			}

			for (int i = 0; i < hand.size(); i++) {
				drawCard(g, hand.get(i), (i + 1) * 100 + 20, 300);
				if (selectedCard == null || !selectedCard.equals(hand.get(i))) {
					g.setColor(new Color(180, 222, 240)); // match background
				} else {
					g.setColor(Color.blue);
				}
				g.drawRoundRect((i + 1) * 100 + 17, 297, 85, 129, 20, 20);
				g.drawRoundRect((i + 1) * 100 + 18, 298, 83, 127, 20, 20);
				g.drawRoundRect((i + 1) * 100 + 19, 299, 81, 125, 20, 20);

			}
			for (int i = 1; i <= numSpoonsWon; i++){
				g.drawImage(spoonImage, i * 60 + 420, 40, 75, 100, this);
			}
			
			

		}

	} // end nested class Display

	// ------------------- Private member variables and methods
	// ---------------------------

	private SpoonsClient connection; // Handles communication with the PokerHub;
										// used to send messages to the hub.

	private SpoonsState state; // Represents the state of the game, as seen by
								// this player. The state is
								// received as a message from the hub whenever
								// the state changes. This
								// variable changes only in the newState()
								// method.

	private Display display; // The content pane of the window, defined by the
								// inner class, Display.

	private Image cardImages; // An image holding pictures of all the cards. The
								// Image is loaded
	Image spoonImage = null;

	private JLabel messageFromServer;
	private JLabel timerString;
	private JLabel timerLabel;
	private JLabel numSpoonsWonLabel;
	private JLabel instructionsLabel;

	String playerName;
	
	SpoonsCard selectedCard;
	int indexOfSelectedCard;
	ArrayList<SpoonsCard> hand;

	boolean canGrabSpoon;
	boolean grabbedSpoon;
	int numSpoonsLeft;
	int numSpoonsWon;
	
	Timer timer;

	int timerCount = 0; // This variable will keep track of how many times the
						// action performed method of timer is called.
						// If timerCount is a multiple of 100 (1 second) the
						// card will be sent to the hub.

	/**
	 * This method is called when a new SpoonsState is received from the
	 * PokerHub. It changes the GUI and the window's state to match the new game
	 * state. The new state is also stored in the instance variable named state.
	 */
	private void newState(SpoonsState state) {

		this.state = state;

		canGrabSpoon = state.canGrabSpoon;
		numSpoonsLeft = state.numSpoonsLeft;
		if (state.hand != null)
			hand = state.hand;
		repaint();

	}

	/**
	 * This method is called when the user clicks the display at the point
	 * (x,y). Clicks are ignored except when the user is selecting cards to
	 * discard (that is when state.status is SpoonsState.DRAW). While
	 * discarding, if the user clicks a card, that card is turned over.
	 */
	private void doClick(int x, int y) {
		if (state == null)
			return;
		for (int i = 0; i < 4; i++) {
			if (y >= 300 && y <= 423 && x >= (i + 1) * 100 + 20
					&& x <= (i + 1) * 100 + 99) {
				// Clicked on card number i. Toggle the value in
				// discard[i]. If the card is face up, it will be
				// selected for discarding and will now appear face down.
				// If the card is already selected for discarding,
				// it will be de-selected.
				selectedCard = hand.get(i);
				indexOfSelectedCard = i;
				repaint();
				break;
			}
		}
		if (y >= 10 && y <= 210 && x >= 90 && x < numSpoonsLeft * 150 + 90) {
			if ((canGrabSpoon || canGrabSpoon()) && !grabbedSpoon) {
				grabbedSpoon = true;
				timer.stop();
				numSpoonsWon++;
				if (numSpoonsWon == 3){
					connection.send("I won 3 spoons_"); //code to end game;
				}
				connection.send("grabbedSpoon");

				messageFromServer.setText("Congratulations! You got a spoon.");
				
				repaint();

			}
		}
	}

	public boolean canGrabSpoon() {
		return (hand.get(0).getValue() == hand.get(1).getValue() && hand.get(1).getValue() == hand.get(2).getValue() &&
				hand.get(2).getValue() == hand.get(3).getValue() && hand.get(3).getValue() == hand.get(0).getValue());
	}

	/**
	 * This method is called when the user clicks the "QUIT" button or closed
	 * the window. The client disconnects from the server before terminating the
	 * program. This will be seen by the Hub, which will inform the other
	 * player's program (if any), so that that program can also terminate.
	 */
	private void doQuit() {
		dispose(); // Close the window.
		if (connection != null) {
			connection.disconnect();
			try { // time for the disconnect message to be sent.
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		System.exit(0);
	}

	/**
	 * Draws a card in a 79x123 pixel rectangle with its upper left corner at a
	 * specified point (x,y). Drawing the card requires the resource file
	 * "netgame/fivecarddraw/cards.png".
	 * 
	 * @param g
	 *            The non-null graphics context used for drawing the card.
	 * @param card
	 *            The card that is to be drawn. If the value is null, then a
	 *            face-down card is drawn.
	 * @param x
	 *            the x-coord of the upper left corner of the card
	 * @param y
	 *            the y-coord of the upper left corner of the card
	 */
	public void drawCard(Graphics g, SpoonsCard card, int x, int y) {
		int cx; // x-coord of upper left corner of the card inside cardsImage
		int cy; // y-coord of upper left corner of the card inside cardsImage
		if (card == null) {
			cy = 4 * 123; // coords for a face-down card.
			cx = 2 * 79;
		} else {
			if (card.getValue() == SpoonsCard.ACE)
				cx = 0;
			else
				cx = (card.getValue() - 1) * 79;
			switch (card.getSuit()) {
			case SpoonsCard.CLUBS:
				cy = 0;
				break;
			case SpoonsCard.DIAMONDS:
				cy = 123;
				break;
			case SpoonsCard.HEARTS:
				cy = 2 * 123;
				break;
			default: // spades
				cy = 3 * 123;
				break;
			}
		}
		g.drawImage(cardImages, x, y, x + 79, y + 123, cx, cy, cx + 79,
				cy + 123, this);
	}

} // end class SpoonsWindow

