package au.unimelb.pattern;

import au.qut.apromore.automaton.Automaton;
import au.qut.apromore.importer.DecodeTandemRepeats;
import com.google.common.collect.BiMap;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

import java.util.List;
import java.util.Map;

public class ReducedTrace
{
    IntArrayList reducedTrace;
    private DecodeTandemRepeats decoder;
    IntIntHashMap finalConfiguration;

    int traceCount;
    Automaton dafsa;
    BiMap<Integer, String> labelMapping;
    BiMap<String, Integer> inverseLabelMapping;
    Map<Integer, String> caseIDs;

    List<RepetitivePattern> repetitivePatterns;

    public ReducedTrace(DecodeTandemRepeats decoder, int traceCount,
                        BiMap<Integer, String> labelMapping, BiMap<String, Integer> inverseLabelMapping, Map<Integer, String> caseIDs)
    {
        this.reducedTrace = decoder.reducedTrace();
        this.finalConfiguration = decoder.getFinalReducedConfiguration();
        this.decoder = new DecodeTandemRepeats(decoder);
        this.traceCount=traceCount;
        this.repetitivePatterns = new FastList<>();
        for(int tandemRepeat=0; tandemRepeat<decoder.reductionStartPositions().size();tandemRepeat++)
        {
            repetitivePatterns.add(new RepetitivePattern(decoder.reductionStartPositions().get(tandemRepeat),decoder.reductionCollapsedLength().get(tandemRepeat)));
        }
        this.labelMapping=labelMapping;
        this.inverseLabelMapping=inverseLabelMapping;
        this.caseIDs=caseIDs;
    }

    public List<RepetitivePattern> getRepetitivePatterns()
    {
        return repetitivePatterns;
    }

    public Automaton getDafsa()
    {
        if(this.dafsa==null) {
            dafsa = new Automaton(reducedTrace, labelMapping, inverseLabelMapping, caseIDs);
        }
        return this.dafsa;
    }

    public DecodeTandemRepeats getDecoder() {
        return decoder;
    }

    public void addToTraceCount(int traceCount)
    {
        this.traceCount+=traceCount;
    }

    public int getTraceCount() {
        return traceCount;
    }
}
