package nl.tue.alignment;

public interface Progress extends Canceler {

  Progress INVISIBLE = new Progress() {

    public void setMaximum(int maximum) {
    }

    public void inc() {
    }

    public boolean isCanceled() {
      return false;
    }

    public void log(String message) {
      System.out.println(message);
    }

  };

  void setMaximum(int maximum);

  void inc();

  void log(String message);
}
