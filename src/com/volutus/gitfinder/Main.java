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
        int numberOfWorkers = 200;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkers);
        for (int i = 0; i<numberOfWorkers; i++)
        {
            FileSearchWorker worker = new FileSearchWorker(i);
            workers.add(worker);
            executor.execute(worker);
        }

        while (working)
        {
            if (directoriesToSearch.size() == 0)
            {
                boolean stillWorking = false;
                for (FileSearchWorker worker: workers)
                {
                    if (worker.isWorking())
                    {
                        stillWorking = true;
                        break;
                    }
                }
                working = stillWorking;
            }
            else
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        executor.shutdown();

        long end = System.nanoTime();
        long run = (end - start) / 1000000;

        System.out.println("Scanned " + directoryCount + " directories in " + run + "ms");
        for (String repo: discoveredRepos)
        {
            System.out.println("Repo exists at " + repo);
        }
        System.out.println("Number of repos found - " + repoCount);
    }
}
