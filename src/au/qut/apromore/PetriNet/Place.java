package au.qut.apromore.PetriNet;

public class Place
{
    private static int ID = 0;
    int id;
    String label;

    public Place()
    {
        id = ID++;
        label = "p" + id;
    }

    public Place(String label)
    {
        id = ID++;
        this.label = label;
        if(this.label==null) this.label = "p" + id;
    }

    public int id()
    { return this.id;}

    public String label()
    { return this.label;}
}
