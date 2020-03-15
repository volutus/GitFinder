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
            File directory = null;
            try
            {
                directory = Main.directoriesToSearch.remove(0);
            }
            catch (Exception e)
            {
                // DO NOTHING
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
                            Main.directoriesToSearch.add(file);
                        }
                    }
                }
                working = false;
            }
        }
    }
}
