

import java.io.IOException;
import java.util.ArrayList;

import netgame.common.*;


public class SpoonsHub extends Hub {
   
   public SpoonsDeck deck;  // The deck of 52 playing cards.
   private ArrayList<SpoonsCard> handToSend;
   int numSpoonsRemaining;
   int numPlayers;
   int numSpoonsTotal;
   boolean nextGame = true;
   String[] names;
   

   /**
    * Creates a SpoonsHub listening on a specified port.
    */
   public SpoonsHub(int port, int players) throws IOException {
      super(port);
      numPlayers = players;
      if (numPlayers % 2 == 0){
    	  numSpoonsRemaining = numPlayers / 2;
      }
      else
    	  numSpoonsRemaining = numPlayers / 2 + 1;
      
      numSpoonsTotal = numSpoonsRemaining;
      names = new String[players];
   }
   

   /**
    * When the second player connects, this method starts the game by
    * sending the initial game state to the two players.  At this time,
    * the players' hands are null.  The hands will be set when the
    * first hand is dealt.  This method also shuts down the Hub's 
    * ServerSocket so that no further players can connect.
    */
   protected void playerConnected(int playerID) {
	  if (playerID < numPlayers){
		  sendToAll("Waiting for " + (numPlayers - playerID) + " more player(s)...");
	  }
      if (playerID == numPlayers) {
    	  shutdownServerSocket();
    	  sendToAll("The game will start in 10 seconds.");
    	  
    	   	try {
    				Thread.sleep(10000);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    	  
    	
         startGame();
      }
   }
   
   private void startGame(){
	   
   	
	   deck = new SpoonsDeck();
	   numSpoonsRemaining = numSpoonsTotal;
	  //System.out.println(names);
	   
	   deck.shuffle();
       for (int i = 1; i <= numPlayers; i++){
      	 handToSend = new ArrayList<SpoonsCard>();
      	 for (int c = 0; c < 4; c++){
      		 handToSend.add(deck.dealCard());
      	 }
      	 sendToOne(i, new SpoonsState(handToSend, false, numSpoonsRemaining));
       	 sendToOne(i, "start game"); //code to start timers
       	 try{
       		 Thread.sleep(250);
       	 }catch(InterruptedException e){
       		 e.printStackTrace();
       	 }
       }
        
       sendToAll("Select the card you would like to discard.");
   }

   
   /**
    * If a player disconnects, the game ends.  This method shuts down
    * the Hub, which will send a signal to the remaining connected player,
    * if any, to let them know that their opponent has left the game.
    * The client will respond by terminating that player's program.
    */
   protected void playerDisconnected(int playerID) {
	   numPlayers--;
	   if (numPlayers <= numSpoonsTotal)
		   shutDownHub();
   }


   /**
    * This is the method that responds to messages received from the
    * clients.  It handles all of the action of the game.  When a message
    * is received, this method will make any changes to the state of
    * the game that are triggered by the message.  It will then send
    * information about the new state to each player, and it will
    * generally send a string to each client as a message to be
    * displayed to that player.
    */
   protected void messageReceived(int playerID, Object message) {
      if (message.equals("grabbedSpoon")) {
    	 numSpoonsRemaining--; 
         sendToAll(new SpoonsState(null, true, numSpoonsRemaining)); //no cards are sent, canGrabSpoon is set to true, one less spoon should exist
         if (numSpoonsRemaining == 0 && nextGame){
        	 sendToAll("end game"); //code to stop timers
        	 try{
        		 Thread.sleep(2000);
        		 
        	 }catch (Exception e){
        		 e.printStackTrace();
        	 }
        	 sendToAll("The next game will start in 7 seconds.");
        	 try{
        		 Thread.sleep(7000);
        		 
        	 }catch (Exception e){
        		 e.printStackTrace();
        	 }
        	 startGame();
        	 
         }
      }
      else if (message.equals("I won 3 spoons_")){
    	  nextGame = false;
    	  sendToAll(names[playerID - 1] + " wins! Thanks for playing.");
    	  shutDownHub();
      }
      else if (message instanceof String){//player name
    	  names[playerID - 1] = (String)message;
      }
      else {  //The message is the card that needs to be replaced. 
    	  sendToOne(playerID, deck.dealCard());
    	  int r = (int)(Math.random() * 3);
    	  deck.addCardToDeck(deck.size() - r, (SpoonsCard)message);
    	  //System.out.println("Card sent to player " + playerID);
      }
   } 
}
