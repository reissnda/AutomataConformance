package org.apromore.alignmentautomaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.signaturediscovery.encoding.ActivityOverFlowException;
import org.processmining.plugins.signaturediscovery.encoding.EncodeActivitySet;
import org.processmining.plugins.signaturediscovery.encoding.EncodeTraces;
import org.processmining.plugins.signaturediscovery.encoding.EncodingNotFoundException;
import org.processmining.plugins.signaturediscovery.encoding.InstanceProfile;
import org.processmining.plugins.signaturediscovery.types.Feature;
import org.processmining.plugins.signaturediscovery.util.EquivalenceClass;
import org.processmining.plugins.signaturediscovery.util.Logger;
import org.processmining.plugins.signaturediscovery.util.UkkonenSuffixTree;

public class TandemRepeatsMain {

  static Map<Feature, Set<String>> originalSequenceFeatureSetMap = new HashMap<Feature, Set<String>>();

  static Map<Feature, Set<String>> baseSequenceFeatureSetMap = new HashMap<Feature, Set<String>>();

  static XLog xLog;

  static int encodingLength;

  static Map<String, String> charActivityMap;

  static Map<String, String> activityCharMap;

  static List<String> encodedTraceList;

  static List<InstanceProfile> instanceProfileList;

//  public static void main(String[] args) throws Exception {
//    String path = "/Users/dreissner/Documents/Paper tests/S-Components Paper/TandemRepeatsTest";
//    String log = "wepaper.xes";
//    ImportEventLog importer = new ImportEventLog();
//    xLog = importer.importEventLog(path + "/" + log);
//
//        /*SignatureDiscoveryInput input = new SignatureDiscoveryInput();
//        input.getSelectedFeatureSet().add(Feature.TR);
//        input.getSelectedFeatureSet().add(Feature.MR);
//        input.getSelectedFeatureSet().add(Feature.SMR);
//        input.getSelectedFeatureSet().add(Feature.NSMR);
//        DiscoverSignatures discoverSignatures = new DiscoverSignatures(xLog, input);*/
//    //System.out.println(discoverSignatures.hasSignatures());
//    encodeLog();
//    Set<Feature> repeatFeatureSet = new HashSet<Feature>();
//    repeatFeatureSet.add(Feature.MR);
//    //repeatFeatureSet.add(Feature.SMR);
//    //repeatFeatureSet.add(Feature.NSMR);
//    int index = 1;
//    System.out.println(charActivityMap);
//    System.out.println(encodedTraceList.get(index));
//    computeRepeatfeatureFeatureSetMap(encodingLength, encodedTraceList.get(index), repeatFeatureSet);
//    System.out.println(originalSequenceFeatureSetMap);
//    System.out.println(baseSequenceFeatureSetMap);
//  }

  private static void computeRepeatfeatureFeatureSetMap(int encodingLength, String charStream,
      Set<Feature> repeatFeatureSet) {
    Logger.printCall("Calling FeatureExtraction->computeRepeatfeatureFeatureSetMap");
    Logger.println(repeatFeatureSet);

    EquivalenceClass equivalenceClass = new EquivalenceClass();
    Map<Set<String>, Set<String>> alphabetPatternEquivalenceClassMap;
    Set<String> alphabetEquivalenceClassPatternSet;

    UkkonenSuffixTree suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
    suffixTree.findLeftDiverseNodes();
    for (Feature feature : repeatFeatureSet) {
      switch (feature) {
        case MR:
          Set<String> maximalRepeatSet = suffixTree.getMaximalRepeats();
          Set<String> baseMaximalRepeatSet = new HashSet<String>();
          alphabetPatternEquivalenceClassMap = equivalenceClass
              .getAlphabetEquivalenceClassMap(encodingLength, maximalRepeatSet);
          for (Set<String> alphabet : alphabetPatternEquivalenceClassMap.keySet()) {
            alphabetEquivalenceClassPatternSet = alphabetPatternEquivalenceClassMap.get(alphabet);
            for (String pattern : alphabetEquivalenceClassPatternSet) {
              if (pattern.length() / encodingLength == alphabet.size()) {
                baseMaximalRepeatSet.add(pattern);
              }
            }
          }

          originalSequenceFeatureSetMap.put(feature, maximalRepeatSet);
          baseSequenceFeatureSetMap.put(feature, baseMaximalRepeatSet);

          break;
        case SMR:
          Set<String> superMaximalRepeatSet = suffixTree.getSuperMaximalRepeats();
          Set<String> baseSuperMaximalRepeatSet = new HashSet<String>();
          alphabetPatternEquivalenceClassMap = equivalenceClass
              .getAlphabetEquivalenceClassMap(encodingLength, superMaximalRepeatSet);
          for (Set<String> alphabet : alphabetPatternEquivalenceClassMap.keySet()) {
            alphabetEquivalenceClassPatternSet = alphabetPatternEquivalenceClassMap.get(alphabet);
            for (String pattern : alphabetEquivalenceClassPatternSet) {
              if (pattern.length() / encodingLength == alphabet.size()) {
                baseSuperMaximalRepeatSet.add(pattern);
              }
            }
          }
          originalSequenceFeatureSetMap.put(feature, superMaximalRepeatSet);
          baseSequenceFeatureSetMap.put(feature, baseSuperMaximalRepeatSet);

          break;
        case NSMR:
          Set<String> nearSuperMaximalRepeatSet = suffixTree.getSuperMaximalRepeats();
          Set<String> baseNearSuperMaximalRepeatSet = new HashSet<String>();
          alphabetPatternEquivalenceClassMap = equivalenceClass
              .getAlphabetEquivalenceClassMap(encodingLength, nearSuperMaximalRepeatSet);
          for (Set<String> alphabet : alphabetPatternEquivalenceClassMap.keySet()) {
            alphabetEquivalenceClassPatternSet = alphabetPatternEquivalenceClassMap.get(alphabet);
            for (String pattern : alphabetEquivalenceClassPatternSet) {
              if (pattern.length() / encodingLength == alphabet.size()) {
                baseNearSuperMaximalRepeatSet.add(pattern);
              }
            }
          }
          originalSequenceFeatureSetMap.put(feature, nearSuperMaximalRepeatSet);
          baseSequenceFeatureSetMap.put(feature, baseNearSuperMaximalRepeatSet);

          break;
      }
    }

    Logger.printReturn("Returning FeatureExtraction->computeRepeatfeatureFeatureSetMap");
  }

  private static void encodeLog() {
    /*
     * activitySet accumulates the set of distinct
     * activities/events in the event log; it doesn't store the trace
     * identifier for encoding; Encoding trace identifier is required only
     * when any of the maximal repeat (alphabet) features is selected
     */

    Set<String> activitySet = new HashSet<String>();
    XAttributeMap attributeMap;
    Set<String> eventTypeSet = new HashSet<String>();

    for (XTrace trace : xLog) {
      for (XEvent event : trace) {
        attributeMap = event.getAttributes();
        activitySet.add(
            attributeMap.get("concept:name").toString() + "-" + attributeMap.get("lifecycle:transition").toString());
        eventTypeSet.add(attributeMap.get("lifecycle:transition").toString());
      }
      //activitySet.add(trace.getAttributes().get("concept:name").toString());
    }

    try {
      EncodeActivitySet encodeActivitySet = new EncodeActivitySet(activitySet);
      encodingLength = encodeActivitySet.getEncodingLength();

      activityCharMap = encodeActivitySet.getActivityCharMap();
      charActivityMap = encodeActivitySet.getCharActivityMap();
      //			System.out.println("Encoding Length: "+encodingLength);
      //			System.out.println("activityCharMap size: "+activityCharMap.size());
      /*
       * Encode each trace to a charStream
       */
      EncodeTraces encodeTraces = new EncodeTraces(activityCharMap, xLog);
      encodedTraceList = encodeTraces.getCharStreamList();
      instanceProfileList = encodeTraces.getInstanceProfileList();
    } catch (ActivityOverFlowException e) {
      e.printStackTrace();
    } catch (EncodingNotFoundException e) {
      e.printStackTrace();
    }
  }
}
