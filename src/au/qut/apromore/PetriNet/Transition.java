package au.qut.apromore.PetriNet;

public class Transition
{
    private static int ID = 0;
    int id;
    String label;
    boolean isVisible;

    public Transition(String label, boolean isVisible)
    {
        id = ID++;
        this.label = label;
        this.isVisible = isVisible;
    }

    public int id()
    { return this.id;}

    public String label()
    { return this.label;}

    public boolean isVisible() {
        return isVisible;
    }
}
