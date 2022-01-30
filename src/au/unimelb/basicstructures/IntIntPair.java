package au.unimelb.basicstructures;

import java.util.Objects;

public class IntIntPair
{
    private int first;
    private int second;

    public IntIntPair(int first, int second)
    {
        this.first=first;
        this.second=second;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond()
    {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntIntPair that = (IntIntPair) o;
        return first == that.first &&
                second == that.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
