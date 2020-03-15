package com.volutus.gitfinder;

import java.io.File;

public class FileSearchWorker implements Runnable
{
    public FileSearchWorker(int id)
    {
        this.id = id;
    }

    private boolean working = false;
    private int id;

    public boolean isWorking() {
        return working;
    }
    public int getId() {
        return id;
    }
    public void setWorking(boolean working) {
        this.working = working;
    }

    public void run()
    {
        while (Main.working)
        {
            // This logic is a joke, but it is the best way I've found to do this. You'll see a LOT of
            // exceptions being caught here and exception handling is expensive in Java, but I can't find
            // a better way to do this.
            File directory = null;
            if (Main.directoriesToSearch.size() > 0)
            {
                try
                {
                    directory = Main.directoriesToSearch.remove(0);
                }
                catch (Exception e)
                {
                    // DO NOTHING
                }
            }


            if (directory != null)
            {
                working = true;
                Main.directoryCount++;

                File[] files = directory.listFiles();

                if (files != null)
                {
                    for (File file: files)
                    {
                        if (file.getName().equals(".git"))
                        {
                            Main.discoveredRepos.add(file.toString());
                            Main.repoCount++;
                        }
                        else if (file.isDirectory())
                        {
                            // TODO Consider this optimization. It cuts the run-time in half, but maybe there's a better way?
                            if (!file.getName().contains("Windows"))
                            {
                                Main.directoriesToSearch.add(file);
                            }
                        }
                    }
                }
            }
            working = false;
        }
    }
}
