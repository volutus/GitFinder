package com.volutus.gitfinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchDaemon implements Runnable
{
	public int directoryCount = 0;
	public final List<String> discoveredRepos = new ArrayList<>();
	public final List<File> directoriesToSearch = Collections.synchronizedList(new ArrayList<>());
    public final List<Integer> activeWorkers = Collections.synchronizedList(new ArrayList<>());

	// I've found the best performance around 200 on my machine.
	private static final int NUMBER_OF_WORKERS = 200;

    // These paths cause the program to take way longer since they're not normal user files
    public static final List<String> BAD_PATHS = Arrays.asList("Windows", "/proc/", "/sys/", "/.wine/", "/dosdevices/", "/.cache/");

	public void run()
	{
		// We want to find all .git files on this PC. To prevent issues with scanning network drives, this program
		// will be restricted to just the origin drive for now, although adding drive letter support may be a neat feature.
		long start = System.nanoTime();

		String workingDirectory = System.getProperty("user.dir");
		Path workingPath = Paths.get(workingDirectory);
		Path root = workingPath.getRoot();
		directoriesToSearch.add(root.toFile());

        try (ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_WORKERS))
        {
            for (int i = 0; i < NUMBER_OF_WORKERS; i++)
            {
                SearchWorker worker = new SearchWorker(this, i);
                activeWorkers.add(i);
                executor.execute(worker);
            }

            // Now we wait for those jobbers to finish.
            synchronized (this)
            {
                this.wait();
            }

            // Terminate the threads we made
            executor.shutdown();
        }
        catch (InterruptedException ie)
        {
            ie.printStackTrace(System.out);
        }

		long end = System.nanoTime();
		long run = (end - start) / 1000000;

		// Now print the results to the console
		System.out.println("Scanned " + directoryCount + " directories in " + run + "ms");
		for (String repo: discoveredRepos)
		{
			System.out.println("Repo exists at " + repo);
		}
		System.out.println("Number of repos found - " + discoveredRepos.size());
	}

    public static void main(String[] args)
    {
        SearchDaemon runner = new SearchDaemon();
        runner.run();
    }
}
