package au.qut.apromore.ScalableConformanceChecker;
import au.qut.apromore.psp.Node;
public class ReducedResult
{
    private Node finalNode;
    private double numStates;
    private double queuedStates;
    private double time;

    public ReducedResult(Node finalNode, double numStates, double queuedStates, double time)
    {
        this.finalNode = finalNode;
        this.numStates = numStates;
        this.queuedStates = queuedStates;
        this.time = time;
    }

    public Node getFinalNode()
    {
        return this.finalNode;
    }

    public double getNumStates()
    {
        return this.numStates;
    }

    public double getNumQueuedStates()
    {
        return this.queuedStates;
    }

    public double getTime()
    {
        return this.time;
    }
}
