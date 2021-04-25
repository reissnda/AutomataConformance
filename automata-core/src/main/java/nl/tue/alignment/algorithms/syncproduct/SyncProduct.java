package nl.tue.alignment.algorithms.syncproduct;

public interface SyncProduct {

  byte LOG_MOVE = 1;
  byte MODEL_MOVE = 2;
  byte SYNC_MOVE = 3;
  byte TAU_MOVE = 4;

  int MAXTRANS = 0b01111111111111111111111111;
  int NOEVENT = -2;
  int NORANK = NOEVENT;

  /**
   * Returns the number of transitions. At most MAXTRANS transitions are allowed
   * for memory reasons. (i.e. valid transition numbers are 0..MAXTRANS-1)
   */
  int numTransitions();

  /**
   * The number of places is in principle bounded Integer.MAX_VALUE
   */
  int numPlaces();

  /**
   * The number of events in the trace
   */
  int numEvents();

  /**
   * Returns a sorted array of places serving as input to transition t
   */
  int[] getInput(int transition);

  /**
   * Returns a sorted array of places serving as output to transition t
   */
  int[] getOutput(int transition);

  /**
   * Return the initial marking as an array where each byte represents the marking
   * of that specific place in the interval 0..3
   */
  byte[] getInitialMarking();

  /**
   * Return the final marking
   */
  byte[] getFinalMarking();

  /**
   * returns the cost of firing t. Note that the maximum cost of an alignment is
   * 16777216, hence costs should not be excessive.
   */
  int getCost(int transition);

  /**
   * Returns the label of transition t
   */
  String getTransitionLabel(int t);

  /**
   * Checks if a given marking is the (a) final marking
   */
  boolean isFinalMarking(byte[] marking);

  /**
   * Return the label of a place
   */
  String getPlaceLabel(int place);

  /**
   * Returns the label of the synchronous product
   */
  String getLabel();

  /**
   * returns the event number associated with this transitions. Events are assumed
   * numbered 0..(getNumEvents()-1) and for model-move transitions, this method
   * returns NOEVENT (getTypeOf should then return MODEL_MOVE or TAU_MOVE)
   */
  int getEventOf(int transition);

  /**
   * returns the rank of the transition. If a transition is a Model Move, the rank
   * should return NORANK. For sync products made from linear traces, the rank
   * should be the event number, i.e. getRankOf returns getEventOf.
   *
   * For SyncProducts of partially ordered traces, the rank should be such that
   * the longest sequence in the trace have consecutive ranks. All other events
   * have a rank equal to the maximum rank of their predecessors.
   */
  int getRankOf(int transition);

  /**
   * returns the type of the transion as a byte equal to one of the constants
   * defined in this class: LOG_MOVE, SYNC_MOVE, MODEL_MOVE, TAU_MOVE
   */
  byte getTypeOf(int transition);

  /**
   * returns the move to which the transition corresponds. If transition is a
   * MODEL_MOVE or TAU_MOVE, the transition itself is returned from 0..(N-1) where
   * N is the numModelMoves(). If the transition is a LOG_MOVE then N..(N+A-1) is
   * returned, where A is numEventClasses() and if the transition is a SYNC_MOVE
   * then (N+A)..(N+2*A-1) is returned.
   */
  int getMoveOf(int transition);

  /**
   * returns the number of event classes known to this product
   */
  int numEventClasses();

  /**
   * returns the number of model moves in this product
   */
  int numModelMoves();
}
