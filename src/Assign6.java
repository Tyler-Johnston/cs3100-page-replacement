import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Assign6
{

    public static int MAX_SIMULATIONS = 1000;
    public static int MAX_FRAMES = 100;
    public static int[][] pageFaultsFIFO = new int[MAX_SIMULATIONS][MAX_FRAMES];
    public static int[][] pageFaultsLRU = new int[MAX_SIMULATIONS][MAX_FRAMES];
    public static int[][] pageFaultsMRU = new int[MAX_SIMULATIONS][MAX_FRAMES];
    public static int fewestFramesFIFO = 0;
    public static int fewestFramesLRU = 0;
    public static int fewestFramesMRU = 0;
    public static ArrayList<String> anomaliesFIFO = new ArrayList<>();
    public static ArrayList<String> anomaliesLRU = new ArrayList<>();
    public static ArrayList<String> anomaliesMRU = new ArrayList<>();

    public static void main(String[] args)
    {

        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long timeStart = System.currentTimeMillis();
        for (int simulation = 0; simulation < MAX_SIMULATIONS; simulation++)
        {
            int[] pageSequence = getRandomSequence();
            for (int frame = 1; frame < MAX_FRAMES+1; frame++)
            {
                Runnable fifo = new TaskFIFO(pageSequence, frame, 250, pageFaultsFIFO[simulation]);
                Runnable lru = new TaskLRU(pageSequence, frame, 250, pageFaultsLRU[simulation]);
                Runnable mru = new TaskMRU(pageSequence, frame, 250, pageFaultsMRU[simulation]);

                threadPool.execute(fifo);
                threadPool.execute(lru);
                threadPool.execute(mru);
            }
        }
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }
        catch (Exception ex)
        {
            System.out.println("Error in waiting for shutdown");
        }
        long timeEnd = System.currentTimeMillis();

        System.out.printf("Simulation took %d ms\n\n", timeEnd-timeStart);
        report();
    }

    // sequence will be of size 1000 and each reference will be between 1-250.
    public static int[] getRandomSequence()
    {
        int[] sequence = new int[1000];
        for (int i=0; i<sequence.length; i++)
        {
            sequence[i] = (int)((Math.random()*(250))+1);
        }
        return sequence;
    }

    // method to loop through each simulation and sub-frame. it calls the count() and detectAnomalies() methods
    public static void countAndDetect()
    {
        for (int simulation = 0; simulation < MAX_SIMULATIONS; simulation++)
        {
            for (int frame = 1; frame < MAX_FRAMES + 1; frame++)
            {
                countFewestFrames(simulation, frame);
                detectAnomalies(simulation, frame);
            }
        }
    }

    // count how many times each algorithm had the fewest frames
    // if they tied then each algorithm is considered the to have the fewest frames
    public static void countFewestFrames(int simulation, int frame)
    {
        if ((pageFaultsFIFO[simulation][frame - 1] == pageFaultsLRU[simulation][frame - 1]) && (pageFaultsFIFO[simulation][frame - 1] == pageFaultsMRU[simulation][frame - 1]) && (pageFaultsLRU[simulation][frame - 1] == pageFaultsMRU[simulation][frame - 1]))
        {
            fewestFramesFIFO++;
            fewestFramesLRU++;
            fewestFramesMRU++;
        }
        else
        {
            if ((pageFaultsFIFO[simulation][frame - 1] < pageFaultsLRU[simulation][frame - 1]) && (pageFaultsFIFO[simulation][frame - 1] < pageFaultsMRU[simulation][frame - 1]))
            {
                fewestFramesFIFO++;
            }
            if ((pageFaultsLRU[simulation][frame - 1] < pageFaultsFIFO[simulation][frame - 1]) && (pageFaultsLRU[simulation][frame - 1] < pageFaultsMRU[simulation][frame - 1]))
            {
                fewestFramesLRU++;
            }
            if ((pageFaultsMRU[simulation][frame - 1] < pageFaultsFIFO[simulation][frame - 1]) && (pageFaultsMRU[simulation][frame - 1] < pageFaultsLRU[simulation][frame - 1]))
            {
                fewestFramesMRU++;
            }
        }
    }

    // detect each anomaly found in each (potential) frame of each algorithm
    public static void detectAnomalies(int simulation, int frame)
    {
        try
        {
            if (pageFaultsFIFO[simulation][frame-1] < pageFaultsFIFO[simulation][frame])
            {
                anomaliesFIFO.add(String.format("        Anomaly detected in simulation #%d - %d PF's @  %d frames vs. %d PF's @  %d frames (Δ%d)", simulation, pageFaultsFIFO[simulation][frame-1], frame, pageFaultsFIFO[simulation][frame], frame + 1, pageFaultsFIFO[simulation][frame] - pageFaultsFIFO[simulation][frame-1]));
            }
        }
        catch(Exception ignored)
        {
        }
        try
        {
            if (pageFaultsLRU[simulation][frame-1] < pageFaultsLRU[simulation][frame])
            {
                anomaliesLRU.add(String.format("        Anomaly detected in simulation #%d - %d PF's @  %d frames vs. %d PF's @  %d frames (Δ%d)", simulation, pageFaultsLRU[simulation][frame-1], frame, pageFaultsLRU[simulation][frame], frame + 1, pageFaultsLRU[simulation][frame] - pageFaultsLRU[simulation][frame-1]));
            }
        } catch (Exception ignored)
        {
        }
        try
        {
            if (pageFaultsMRU[simulation][frame-1] < pageFaultsMRU[simulation][frame])
            {
                anomaliesMRU.add(String.format("        Anomaly detected in simulation #%d - %d PF's @  %d frames vs. %d PF's @  %d frames (Δ%d)", simulation, pageFaultsMRU[simulation][frame-1], frame, pageFaultsMRU[simulation][frame], frame + 1, pageFaultsMRU[simulation][frame] - pageFaultsFIFO[simulation][frame-1]));
            }
        } catch (Exception ignored)
        {
        }
    }

    // report the anomalies detected. also keeps track of the max delta found
    public static void reportAnomalies(ArrayList<String> anomalies, String algorithm)
    {
        int count = 0;
        int maxDelta = 0;

        System.out.println("\nBelady's Anomaly Report for " + algorithm);

        for (String anomaly : anomalies)
        {
            System.out.println(anomaly);
            count++;
            int delta = Integer.parseInt(String.valueOf(anomaly.charAt(anomaly.length()-2)));
            if (delta > maxDelta)
            {
                maxDelta = delta;
            }
        }
        System.out.println("  Anomaly detected " + count + " times in 1000 simulations with a max delta of " + maxDelta);
    }

    // summarizes the results discovered
    public static void report()
    {
        countAndDetect();
        System.out.println("FIFO min PF : " + fewestFramesFIFO);
        System.out.println("LRU min PF  : " + fewestFramesLRU);
        System.out.println("MRU min PF  : " + fewestFramesMRU);

        // report the anomalies found
        reportAnomalies(anomaliesFIFO, "FIFO");
        reportAnomalies(anomaliesLRU, "LRU");
        reportAnomalies(anomaliesMRU, "MRU");
    }
}

