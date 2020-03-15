package com.volutus.gitfinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main
{
    static int repoCount = 0;
    static int directoryCount = 0;
    static List<String> discoveredRepos = new ArrayList<>();
    static List<File> directoriesToSearch = Collections.synchronizedList(new ArrayList<>());
    static boolean working = true;

    // I've found the best performance around 200 on my machine.
    private static final int NUMBER_OF_WORKERS = 200;

    private static final int POLLING_INTERVAL_IN_MILLIS = 100;

    public static void main(String[] args)
    {
        // We want to find all .git files on this PC. To prevent issues with scanning network drives, this program
        // will be restricted to just the origin drive for now, although adding drive letter support may be a neat
        // feature.
        long start = System.nanoTime();

        String workingDirectory = System.getProperty("user.dir");
        Path workingPath = Paths.get(workingDirectory);
        Path root = workingPath.getRoot();
        directoriesToSearch.add(root.toFile());

        List<FileSearchWorker> workers = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS);
        for (int i = 0; i<NUMBER_OF_WORKERS; i++)
        {
            FileSearchWorker worker = new FileSearchWorker();
            workers.add(worker);
            executor.execute(worker);
        }

        while (working)
        {
            if (directoriesToSearch.size() == 0)
            {
                boolean workerStillActive = false;
                for (FileSearchWorker worker: workers)
                {
                    if (worker.isWorking())
                    {
                        workerStillActive = true;
                        break;      // no point to keep searching
                    }
                }
                working = workerStillActive;
            }
            else
            {
                try
                {
                    // We'll use a polling approach to keep checking our workers.
                    Thread.sleep(POLLING_INTERVAL_IN_MILLIS);
                }
                catch (Exception e)
                {
                    // This really shouldn't ever happen
                    e.printStackTrace();
                }
            }
        }

        // Terminate the threads we made
        executor.shutdown();

        long end = System.nanoTime();
        long run = (end - start) / 1000000;

        // Now print the results to the console
        System.out.println("Scanned " + directoryCount + " directories in " + run + "ms");
        for (String repo: discoveredRepos)
        {
            System.out.println("Repo exists at " + repo);
        }
        System.out.println("Number of repos found - " + repoCount);
    }
}
