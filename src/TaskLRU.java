import java.util.HashMap;
import java.util.HashSet;

public class TaskLRU implements Runnable
{
    int[] sequence;
    int maxMemoryFrames;
    int maxPageReference;
    int[] pageFaults;
    HashSet<Integer> frameTable = new HashSet<>(maxMemoryFrames);
    HashMap<Integer, Integer> indexes = new HashMap<>();

    public TaskLRU(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults)
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
                    int leastRecentlyUsedIndex = Integer.MAX_VALUE;
                    int value = Integer.MIN_VALUE;

                    // find the least recently used value by finding the smallest index in the hashmap
                    // preserve the value (the reference in the sequence) so it can be removed from frameTable and the index hashmap after LRU value is found
                    for (int potentialVictim : frameTable)
                    {
                        if (indexes.get(potentialVictim) != null)
                        {
                            if (indexes.get(potentialVictim) < leastRecentlyUsedIndex)
                            {
                                leastRecentlyUsedIndex = indexes.get(potentialVictim);
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