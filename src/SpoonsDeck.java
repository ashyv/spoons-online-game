import java.util.ArrayList;



/**
 *  An object of type SpoonsDeck represents a deck of playing cards.  The deck
 *  is a regular poker deck that contains 52 regular cards.
 */
public class SpoonsDeck {
   
   /**
    * An array of 52 cards.
    */
   private ArrayList<SpoonsCard> deck;
   
   /**
    * Keeps track of the number of cards that have been dealt from
    * the deck so far.
    */
   private int cardsUsed;
   
   /**
    * Constructs a regular 52-card poker deck.  Initially, the cards
    * are in a sorted order.  The shuffle() method can be called to
    * randomize the order.  
    */
   public SpoonsDeck() {
	   deck = new ArrayList<SpoonsCard>();
	   int cardCt = 0; // How many cards have been created so far.
	   for ( int suit = 0; suit <= 3; suit++ ) {
	      for ( int value = 2; value <= 14; value++ ) {
	    	  deck.add(cardCt, new SpoonsCard(value,suit)); 
	    	  cardCt++;
	      }
	          
	   }
	   cardsUsed = 0;
   }
   
  
   /**
    * Put all the used cards back into the deck (if any), and
    * shuffle the deck into a random order.
    */
   public void shuffle() {
      for ( int i = deck.size()-1; i > 0; i-- ) {
         int rand = (int)(Math.random()*(i+1));
         SpoonsCard temp = deck.get(i);
         deck.set(i, deck.get(rand));
         deck.set(rand, temp);
      }
      cardsUsed = 0;
   }
   
   
   /**
    * Removes the next card from the deck and return it.  It is illegal
    * to call this method if there are no more cards in the deck.  You can
    * check the number of cards remaining by calling the cardsLeft() function.
    * @return the card which is removed from the deck.
    * @throws IllegalStateException if there are no cards left in the deck
    */
   public SpoonsCard dealCard() {
	  SpoonsCard card = deck.get(0);
	  deck.remove(0);
	  return card;
   }
   
   /**
    * When the player chooses the card to discard, it will be added back into the deck, at the end. 
    */
   public void addCardToDeck(int index, SpoonsCard card){
	   deck.add(index, card);
   }
   
   public int size(){
	   return deck.size();
   }
   
} // end class SpoonsDeck
