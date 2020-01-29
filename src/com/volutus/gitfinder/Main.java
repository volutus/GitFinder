package com.volutus.gitfinder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args)
    {
	    // We want to find all .git files on this PC. To prevent issues with scanning network drives, this program
        // will be restricted to just the origin drive for now, although adding drive letter support may be a neat
        // feature.
        long start = System.nanoTime();

        String workingDirectory = System.getProperty("user.dir");
        Path workingPath = Paths.get(workingDirectory);
        Path root = workingPath.getRoot();

        List<String> filesSearched = new ArrayList<>();

        // Now that we have root, work our way through the file system and scan for any .git files
        int repoCount = 0;
        int directoryCount = 0;
        List<File> directoriesToSearch = Collections.synchronizedList(new ArrayList<>());
        directoriesToSearch.add(root.toFile());
        while (!directoriesToSearch.isEmpty())
        {
            directoryCount++;
            File directory = directoriesToSearch.remove(0);
            filesSearched.add(directory.getAbsolutePath());
            File[] files = directory.listFiles();

            if (files != null)
            {
                for (File file: files)
                {
                    if (file.getName().equals(".git"))
                    {
                        System.out.println("Repo found in: " + file);
                        repoCount++;
                    }
                    else if (file.isDirectory())
                    {
                        // TODO Consider this optimization. It cuts the run-time in half, but maybe there's a better way?
                        if (!file.getName().contains("Windows"))
                        {
                            directoriesToSearch.add(file);
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();
        long run = (end - start) / 1000000;
        System.out.println("Scanned " + directoryCount + " directories in " + run + "ms");
        System.out.println("Repos found: " + repoCount);

    }
}
