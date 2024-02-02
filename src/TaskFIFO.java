import java.util.LinkedList;

public class TaskFIFO implements Runnable
{
    int[] sequence;
    int maxMemoryFrames;
    int maxPageReference;
    int[] pageFaults;
    LinkedList<Integer> queue = new LinkedList<Integer>();

    public TaskFIFO(int[] sequence, int maxMemoryFrames, int maxPageReference, int[] pageFaults)
    {
        this.sequence = sequence;
        this.maxMemoryFrames = maxMemoryFrames;
        this.maxPageReference = maxPageReference;
        this.pageFaults = pageFaults;
    }

    public int getPageFaultCount()
    {
        int pageFaultCount = 0;

        for (int reference : sequence)
        {
            if (queue.size() < maxMemoryFrames)
            {
                if (!queue.contains(reference))
                {
                    queue.add(reference);
                    pageFaultCount++;
                }
            }
            else
            {
                if (!queue.contains(reference))
                {
                    queue.poll();
                    queue.add(reference);
                    pageFaultCount++;
                }
            }
        }
        return pageFaultCount;
    }


    @Override
    public void run()
    {
        pageFaults[maxMemoryFrames-1] = getPageFaultCount();
    }
}