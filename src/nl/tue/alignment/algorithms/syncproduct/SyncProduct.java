package nl.tue.alignment.algorithms.syncproduct;

public interface SyncProduct {

	public static byte LOG_MOVE = 1;
	public static byte MODEL_MOVE = 2;
	public static byte SYNC_MOVE = 3;
	public static byte TAU_MOVE = 4;

	public static final int MAXTRANS = 0b01111111111111111111111111;
	public static final int NOEVENT = -2;
	public static final int NORANK = NOEVENT;

	/**
	 * Returns the number of transitions. At most MAXTRANS transitions are allowed
	 * for memory reasons. (i.e. valid transition numbers are 0..MAXTRANS-1)
	 * 
	 * @return
	 */
	public int numTransitions();

	/**
	 * The number of places is in principle bounded Integer.MAX_VALUE
	 * 
	 * @return
	 */
	public int numPlaces();

	/**
	 * The number of events in the trace
	 * 
	 * @return
	 */
	public int numEvents();

	/**
	 * Returns a sorted array of places serving as input to transition t
	 * 
	 * @param transition
	 * @return
	 */
	public int[] getInput(int transition);

	/**
	 * Returns a sorted array of places serving as output to transition t
	 * 
	 * @param transition
	 * @return
	 */
	public int[] getOutput(int transition);

	/**
	 * Return the initial marking as an array where each byte represents the marking
	 * of that specific place in the interval 0..3
	 * 
	 * @return
	 */
	public byte[] getInitialMarking();

	/**
	 * Return the final marking
	 * 
	 * @return
	 */
	public byte[] getFinalMarking();

	/**
	 * returns the cost of firing t. Note that the maximum cost of an alignment is
	 * 16777216, hence costs should not be excessive.
	 * 
	 * 
	 * @param t
	 * @return
	 */
	public int getCost(int transition);

	/**
	 * Returns the label of transition t
	 * 
	 * @param t
	 * @return
	 */
	public String getTransitionLabel(int t);

	/**
	 * Checks if a given marking is the (a) final marking
	 * 
	 * @param marking
	 * @return
	 */
	public boolean isFinalMarking(byte[] marking);

	/**
	 * Return the label of a place
	 * 
	 * @param p
	 * @return
	 */
	public String getPlaceLabel(int place);

	/**
	 * Returns the label of the synchronous product
	 * 
	 * @return
	 */
	public String getLabel();

	/**
	 * returns the event number associated with this transitions. Events are assumed
	 * numbered 0..(getNumEvents()-1) and for model-move transitions, this method
	 * returns NOEVENT (getTypeOf should then return MODEL_MOVE or TAU_MOVE)
	 * 
	 * @param transition
	 * @return
	 */
	public int getEventOf(int transition);

	/**
	 * returns the rank of the transition. If a transition is a Model Move, the rank
	 * should return NORANK. For sync products made from linear traces, the rank
	 * should be the event number, i.e. getRankOf returns getEventOf.
	 * 
	 * For SyncProducts of partially ordered traces, the rank should be such that
	 * the longest sequence in the trace have consecutive ranks. All other events
	 * have a rank equal to the maximum rank of their predecessors.
	 * 
	 * @param transition
	 * @return
	 */
	public int getRankOf(int transition);

	/**
	 * returns the type of the transion as a byte equal to one of the constants
	 * defined in this class: LOG_MOVE, SYNC_MOVE, MODEL_MOVE, TAU_MOVE
	 * 
	 * @param transition
	 * @return
	 */
	public byte getTypeOf(int transition);

	/**
	 * returns the move to which the transition corresponds. If transition is a
	 * MODEL_MOVE or TAU_MOVE, the transition itself is returned from 0..(N-1) where
	 * N is the numModelMoves(). If the transition is a LOG_MOVE then N..(N+A-1) is
	 * returned, where A is numEventClasses() and if the transition is a SYNC_MOVE
	 * then (N+A)..(N+2*A-1) is returned.
	 * 
	 * @param transition
	 * @return
	 */
	public int getMoveOf(int transition);

	/**
	 * returns the number of event classes known to this product
	 * 
	 * @return
	 */
	public int numEventClasses();

	/**
	 * returns the number of model moves in this product
	 * 
	 * @return
	 */
	public int numModelMoves();
}
