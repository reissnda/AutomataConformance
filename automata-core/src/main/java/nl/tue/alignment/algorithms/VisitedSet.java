package nl.tue.alignment.algorithms;

public interface VisitedSet {

  /**
   * Return the algorithm for which this closedSet is used.
   */
  ReplayAlgorithm getAlgorithm();

  /**
   * Add a marking to the set of visited markings.
   *
   * If it already exists, return it's number, otherwise add it and return
   * newIndex
   *
   * @param marking
   *     the byte[] indicating the number of tokens per place
   * @return the id of the marking in the set. If the marking existed before, the
   *     existing ID is returned, otherwise a new successive ID is returned.
   */
  int add(byte[] marking, int newIndex);

  /**
   * returns the maximum memory use in bytes the set ever had.
   */
  long getEstimatedMemorySize();

  /**
   * empties the set and restores it to its original state
   */
  void clear();

  /**
   * returns the current capacity of the set, including the non-used slots.
   */
  int capacity();

}
