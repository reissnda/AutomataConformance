package main;

import org.eclipse.collections.impl.map.mutable.UnifiedMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CAISE21parser
{
    public static void main(String[] args) throws Exception {
        String[] folders = new String[]
                {
                //"/Users/dreissner/Dropbox/CAISE21-Files/ResultsFitnessIMInfreq/",
                //"/Users/dreissner/Dropbox/CAISE21-Files/ResultsPerfectFitnessIM/",
                "/Users/dreissner/Dropbox/CAISE21-Files/ResultsFitnessIMInfreq-NEW(FitnessWithNewLog)/"
                //"/Users/dreissner/Dropbox/CAISE21-Files/ProvateLogsInfrequentIM/"
        };

        for(String folder : folders)
        {
            File folderFile = new File(folder);
            /*Part for creating the result excel sheet
            String headlines1 = ",Fitness,,,,,,Precision,";
            String headlines2 = "Dataset,Original 0%,Original 20%,Original 40%,Relabeled 0%,Relabeled 20%,Relabeled 40%," +
                    "Original 0%,Original 20%,Original 40%,Relabeled 0%,Relabeled 20%,Relabeled 40%";
            String[] keyValues = new String[]{"Original0.0","Original0.2","Original0.4",
                    "Relabeled0.0","Relabeled0.2","Relabeled0.4"};
            FileWriter pw = new FileWriter(folder + "ExperimentResults_" + folder.split("/")[5] + ".csv");
            pw.append(folder.split("/")[5] + "\n");
            pw.append(headlines1 + "\n");
            pw.append(headlines2 + "\n");
             */
            //CSVOutput for latticecount next
            String headlines = "Dataset,Lattice count 0%,Lattice count 20%, Lattice count 40%";
            String[] keys = new String[]{"0.0","0.2","0.4"};
            FileWriter pwL = new FileWriter(folder + "LatticeCounts_" + folder.split("/")[5] + ".csv");
            pwL.append(folder.split("/")[5] + "\n");
            pwL.append(headlines + "\n");


            //if(pw==null) pw = new FileWriter(path + "log_stats.txt", true);
            System.out.println(folderFile.getAbsolutePath());
            for (File experimentFolder : folderFile.listFiles())
            {
                if(!experimentFolder.isDirectory()||experimentFolder.getName().equals(".DS_Store")) continue;
                System.out.println(experimentFolder.getName());// + " = .DS_Store? " + (experimentFolder.getName().equals(".DS_Store")));
                for(File experimentFile : experimentFolder.listFiles())
                {
                    if(experimentFile.isDirectory()) continue;

                    /*if(experimentFile.getName().equals("fitPrec.txt"))
                    {
                        Scanner myReader = new Scanner(experimentFile);
                        String model = "";
                        UnifiedMap<String,String> fitnessValues = new UnifiedMap<>();
                        UnifiedMap<String,String> precisionValues = new UnifiedMap<>();
                        while (myReader.hasNextLine())
                        {
                            String dataRow = myReader.nextLine();
                            //System.out.println(dataRow);
                            String[] dataColumns =  dataRow.split(",");
                            model = dataColumns[0];
                            String threshold = dataColumns[1];
                            String fitness = dataColumns[2];
                            fitnessValues.put(model + threshold,fitness);
                            String precision = dataColumns[3];
                            precisionValues.put(model + threshold,precision);
                            System.out.println(model +","+threshold+","+fitness+","+precision);
                        }
                        String result = experimentFolder.getName() + ",";
                        for(String key : keyValues)
                        {
                            if(fitnessValues.containsKey(key))
                                result+=fitnessValues.get(key)+",";
                            else
                                result+=",";
                        }
                        for(String key : keyValues)
                        {
                            if(precisionValues.containsKey(key))
                                result+=precisionValues.get(key)+",";
                            else
                                result+=",";
                        }
                        result+="\n";
                        pw.append(result);
                    }*/
                    if(experimentFile.getName().equals("summary.txt"))
                    {
                        Scanner myReader = new Scanner(experimentFile);
                        String threshold = "";
                        UnifiedMap<String,String> latticeCounts = new UnifiedMap<>();
                        while (myReader.hasNextLine())
                        {
                            String dataRow = myReader.nextLine();
                            //System.out.println(dataRow);
                            String[] dataColumns =  dataRow.split(",");
                            threshold = dataColumns[0];
                            String latticeCount = dataColumns[1];
                            latticeCounts.put(threshold,latticeCount);
                            System.out.println(threshold+","+latticeCount);
                        }
                        String result = experimentFolder.getName() + ",";
                        for(String key : keys)
                        {
                            if(latticeCounts.containsKey(key))
                                result+=latticeCounts.get(key)+",";
                            else
                                result+=",";
                        }
                        result+="\n";
                        pwL.append(result);
                    }
                }
            }
            System.out.println();
            pwL.close();
            //pw.close();
        }
    }
}
