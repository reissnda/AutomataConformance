package au.unimelb.oracles;

import au.qut.apromore.importer.GeneralizationImporter;

import java.util.concurrent.*;

public class TestOracle implements Callable<String>
{
    private String path;
    private String log;
    public static void main(String[] args) throws Exception
    {
        //String path = "/Users/dreissner/Documents/evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/AntiAlignmentsDataset/";
        //String path =   "/Users/dreissner/Documents/Evaluations/PatternBasedGeneralizationPaper/QualitativeEvaluation/NegativeEventsDataset/";
        String path = "/Users/dreissner/Documents/Evaluations/TandemRepeatsPaper/public/SM/";
        //String log = "artificial.xes.gz";
        String log;// = "13.xes.gz";
        boolean useGlobal=false;
        if(useGlobal)
            System.out.println("Logfile,applyAlphaPlusPlus,Noise threshold,#Pairs Abel,#Pairs My implementation, #Pairs my implementation with noise filtering");
        else
            System.out.println("LogFile,time,OccurenceThreshold,BalanceThreshold,#Configurations,Avg.#ConcurrentPairs");
        for(int i=1;i<18;i++) {
            //System.out.println("--------------------------");
            log= i + ".xes.gz";
            System.out.print(log+",");
            //GeneralizationImporter generalizationImporter = new GeneralizationImporter();
            //generalizationImporter.testConcurrencyOracle(path + log, false);
            ExecutorService executor = Executors.newCachedThreadPool();
            TestOracle task = new TestOracle(path,log);
            Future<String> future = executor.submit(task);
            String result=null;
            try {
                long start = System.nanoTime();
                result = future.get(2, TimeUnit.MINUTES);
                System.out.print(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start)+",");
            } catch (TimeoutException ex) {
                // handle the timeout
                result="t/out";
            } catch (InterruptedException e) {
                // handle the interrupts
                result=e.getMessage();
            } catch (ExecutionException e) {
                // handle other exceptions
                result=e.getMessage();
            } finally {
                future.cancel(true);  // may or may not desire this
            }
            executor.shutdownNow();
            while(!executor.isShutdown()) executor.awaitTermination(1000,TimeUnit.MILLISECONDS);
            //Thread.sleep(60000);
            System.out.println(result);
        }
    }

    public TestOracle(String path, String log) throws Exception
    {
        this.path=path;
        this.log = log;
    }


    @Override
    public String call() throws Exception {
        String result="";
        GeneralizationImporter generalizationImporter = new GeneralizationImporter();
        result=generalizationImporter.testConcurrencyOracle(path+log, 0.55f,0.3f);
        return result;
    }
}
