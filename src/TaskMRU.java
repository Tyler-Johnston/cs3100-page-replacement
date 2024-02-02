import java.util.HashMap;
import java.util.HashSet;

public class TaskMRU implements Runnable
{
    int[] sequence;
    int maxMemoryFrames;
    int maxPageReference;
    int[] pageFaults;
    HashSet<Integer> frameTable = new HashSet<>(maxMemoryFrames);
    HashMap<Integer, Integer> indexes = new HashMap<>();

    public TaskMRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults)
    {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    public int getPageFaultCount()
    {
        int pageFaults = 0;
        for (int i = 0; i < sequence.length; i++)
        {
            if (frameTable.size() < maxMemoryFrames)
            {
                if (!frameTable.contains(sequence[i]))
                {
                    frameTable.add(sequence[i]);
                    pageFaults++;
                }
                indexes.put(sequence[i], i);
            }
            else
            {
                if (!frameTable.contains(sequence[i]))
                {
                    int mostRecentlyUsedIndex = Integer.MIN_VALUE;
                    int value = Integer.MAX_VALUE;

                    // find the most recently used value by finding the largest index in the hashmap
                    // preserve the value (the reference in the sequence) so it can be removed from frameTable and the index hashmap after MRU value is found
                    for (int potentialVictim : frameTable)
                    {
                        if (indexes.get(potentialVictim) != null)
                        {
                            if (indexes.get(potentialVictim) > mostRecentlyUsedIndex)
                            {
                                mostRecentlyUsedIndex = indexes.get(potentialVictim);
                                value = potentialVictim;
                            }
                        }
                    }
                    frameTable.remove(value);
                    indexes.remove(value);
                    frameTable.add(sequence[i]);
                    pageFaults++;
                }
                indexes.put(sequence[i], i);
            }
        }
        return pageFaults;
    }

    @Override
    public void run()
    {
        pageFaults[maxMemoryFrames-1] = getPageFaultCount();

    }
}