package org.apromore.util.attrfilter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import com.google.common.collect.Sets;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.xeslite.external.XFactoryExternalStore.InMemoryStoreImpl;
import org.xeslite.parser.XesLiteXmlParser;

public final class AttrRemover {

  public static XLog removeAttributes(Set<String> attrNamesToKeep, XLog xLog) {

    for (XTrace trace : xLog) {
      processAttributes(trace, attrNamesToKeep);
    }

    return xLog;
  }

  public static XLog removeAttributes(Set<String> attrNamesToKeep, File logF) throws IOException {

    XesLiteXmlParser parser = new XesLiteXmlParser(new InMemoryStoreImpl(), true);

    try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(logF.toPath()))) {
      XLog xLog = parser.parse(is).get(0);
      return removeAttributes(attrNamesToKeep, xLog);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private static void processAttributes(XAttributable attributable, Set<String> attrNamesToKeep) {
    XAttributeMap attributes = attributable.getAttributes();
    attributes.keySet().removeAll(Sets.difference(attrNamesToKeep, attributes.keySet()));
  }
}
