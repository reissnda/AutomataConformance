package nl.tue.alignment.algorithms;

public interface Queue {

  /**
   * Return the algorithm for which this Queue is used.
   */
  ReplayAlgorithm getAlgorithm();

  /**
   * Show the number of the marking at the head of the priority queue
   */
  int peek();

  /**
   * remove and return the head of the queue
   */
  int poll();

  /**
   * add a new marking to the queue. If it exists and the new score is better,
   * update the score.
   *
   * @return true if the marking was added to the priority queue
   */
  boolean add(int marking);

  /**
   * returns true if the queue is empty
   */
  boolean isEmpty();

  /**
   * returns the maximum memory use in bytes the queue ever had.
   */
  long getEstimatedMemorySize();

  /**
   * returns the maximum memory capacity the queue ever had.
   */
  int maxCapacity();

  /**
   * returns maximum number of elements the queue ever contained.
   */
  int maxSize();

  /**
   * Returns the current number of elements in the queue
   */
  int size();

  /**
   * Checks if the the stored marking with ID markingId is contained in this
   * queue.
   *
   * @return true if the given marking is in the queue
   */
  boolean contains(int markingId);

  /**
   * Debugging method that checks the queue invariant on the queue
   */
  boolean checkInv();
}
