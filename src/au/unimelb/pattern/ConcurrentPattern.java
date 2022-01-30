package au.unimelb.pattern;

import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C1;
import static nl.tue.storage.hashing.impl.MurMur3HashCodeProvider.C2;

public class ConcurrentPattern
{
    Integer hashCode = null;
    IntArrayList tracePositions;
    IntHashSet concurrentLabels;
    int maxFulfillment;
    int partialFulFillment;
    double fulfillment;

    public ConcurrentPattern(IntArrayList tracePositions, IntHashSet concurrentLabels)
    {
        this.tracePositions = tracePositions;
        this.concurrentLabels = concurrentLabels;
        this.maxFulfillment = concurrentLabels.size();
    }

    public void setPartialFulFillment(int partialFulFillment)
    {
        this.partialFulFillment = partialFulFillment;
        this.fulfillment = ((double) partialFulFillment) / maxFulfillment;
    }

    public void setFulfillment(double fulfillment)
    {
        this.fulfillment=fulfillment;
    }

    public IntArrayList tracePositions() {
        return this.tracePositions;
    }

    public IntHashSet getConcurrentLabels(){
        return this.concurrentLabels;
    }

    public double getFulfillment() {
        return fulfillment;
    }



    @Override
    public int hashCode()
    {
        if(hashCode==null)
            hashCode=compress();
        return hashCode;
    }

    private int compress()
    {
        int[] data = new int[2];
        data[0] = compress(tracePositions);
        data[1] = compress(concurrentLabels);

        //data[2] = this.stateModelrep;
        int hash =0;
        final int len = data.length;

        int k1;
        for (int i = 0; i < len - 1; i++) {
            // little endian load order
            k1 = data[i];
            k1 *= C1;

            k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
            k1 *= C2;

            hash ^= k1;

            hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
            hash = hash * 5 + 0xe6546b64;

        }

        // tail
        k1 = data[len - 1];
        k1 *= C1;
        k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
        k1 *= C2;
        hash ^= k1;

        // finalization
        hash ^= len;

        // fmix(h1);
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;

        return hash;
    }

        private int compress(IntArrayList list)
        {
            int[] data = new int[list.size()];
            for(int i=0;i<list.size();i++)
                data[i]=list.get(i);

            //data[2] = this.stateModelrep;
            int hash =0;
            final int len = data.length;

            int k1;
            for (int i = 0; i < len - 1; i++) {
                // little endian load order
                k1 = data[i];
                k1 *= C1;

                k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
                k1 *= C2;

                hash ^= k1;

                hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
                hash = hash * 5 + 0xe6546b64;

            }

            // tail
            k1 = data[len - 1];
            k1 *= C1;
            k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
            k1 *= C2;
            hash ^= k1;

            // finalization
            hash ^= len;

            // fmix(h1);
            hash ^= hash >>> 16;
            hash *= 0x85ebca6b;
            hash ^= hash >>> 13;
            hash *= 0xc2b2ae35;
            hash ^= hash >>> 16;

            return hash;
        }

    private int compress(IntHashSet set)
    {
        int[] data = new int[set.size()];
        int pos=0;
        for(int label : set.toArray())
            data[pos++]=label;

        //data[2] = this.stateModelrep;
        int hash =0;
        final int len = data.length;

        int k1;
        for (int i = 0; i < len - 1; i++) {
            // little endian load order
            k1 = data[i];
            k1 *= C1;

            k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
            k1 *= C2;

            hash ^= k1;

            hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
            hash = hash * 5 + 0xe6546b64;

        }

        // tail
        k1 = data[len - 1];
        k1 *= C1;
        k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
        k1 *= C2;
        hash ^= k1;

        // finalization
        hash ^= len;

        // fmix(h1);
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;

        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        if(object.getClass()!=ConcurrentPattern.class) return false;
        ConcurrentPattern concurrentPattern = (ConcurrentPattern) object;
        return concurrentPattern.hashCode()==this.hashCode();
    }
}
