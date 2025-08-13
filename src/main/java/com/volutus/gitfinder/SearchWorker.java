package com.volutus.gitfinder;

import java.io.File;

public class SearchWorker implements Runnable
{
    private final SearchDaemon daemon;
    private final Integer id;

    public SearchWorker(SearchDaemon daemon, Integer id)
    {
        this.daemon = daemon;
        this.id = id;
    }

	public void run()
	{
        boolean working = true;
		// This will run constantly until the main thread is done, at which point it will resolve nicely.
		while (working)
		{
            File directory = null;
            synchronized (daemon.directoriesToSearch)
            {
                working = !daemon.directoriesToSearch.isEmpty();
                if (working)
                {
                    directory = daemon.directoriesToSearch.removeFirst();
                }
            }
            this.handleDirectory(directory);
            if (!working)
            {
                synchronized (daemon)
                {
                    daemon.activeWorkers.remove(this.id);
                    if (daemon.activeWorkers.isEmpty())
                    {
                        daemon.notify();
                    }
                }
            }
		}
	}

    private void handleDirectory(File directory)
    {
        if (directory == null)
        {
            return;
        }
        daemon.directoryCount++;

        File[] files = directory.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.getName().equals(".git"))
            {
                daemon.discoveredRepos.add(file.toString());
            }
            else if (file.isDirectory())
            {
                // Avoid some bad paths that we don't want to index.
                if (SearchDaemon.BAD_PATHS.stream().noneMatch(file.getPath()::contains))
                {
                    daemon.directoriesToSearch.add(file);
                }
            }
        }
    }
}
