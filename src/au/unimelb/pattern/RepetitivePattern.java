package au.unimelb.pattern;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

public class RepetitivePattern
{
    int startPos;
    int length;
    int maxFulFillment;
    int partialFulfillment;
    double fulfillment;

    public RepetitivePattern(int startPos, int length)
    {
        this.startPos=startPos;
        this.length=length;
        this.maxFulFillment=length/2;
    }

    public void setPartialFulfillment(int partialFulfillment)
    {
        this.partialFulfillment=partialFulfillment;
        this.fulfillment=((double) partialFulfillment)/maxFulFillment;
    }

    public int getStartPos()
    {
        return this.startPos;
    }

    public int getLength() {
        return length;
    }

    public double getFulfillment() {
        return fulfillment;
    }

    public int getMaxFulFillment() { return maxFulFillment; }
}
