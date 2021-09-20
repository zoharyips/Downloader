package com.example;

import okhttp3.OkHttpClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The main downloader
 */
public class Downloader {

    private final OkHttpClient okHttpClient;
    private static volatile Downloader INSTANCE;

    private Downloader() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * There is only one downloader in the whole program
     */
    public static Downloader getInstance() {
        if (INSTANCE == null) {
            synchronized (Downloader.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Downloader();
                }
            }
        }
        return INSTANCE;
    }

    public static void download(String url, String filePath, int partitions) throws IOException, ExecutionException, InterruptedException {
        /* generate the download task */
        Task downloadTask = new Task(url, partitions);

        long avg = downloadTask.getAvg(), beg = 0, end = avg;
        List<Future<PartialRes>> resList = new ArrayList<>(partitions);
        ExecutorService stealingPool = Executors.newWorkStealingPool();

        /* split into partial tasks */
        for (int i = 0; i < partitions; i++, beg = end, end = i == partitions - 1 ? downloadTask.getLen() : end + avg) {
            PartialTask task = new PartialTask(downloadTask, i, beg, end);
            resList.add(stealingPool.submit(task));
        }

        /* read from task */
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            for (Future<PartialRes> resFuture : resList) {
                // TODO: bug to fix - the first task is never started.
                byte[] partialBuffer = resFuture.get().getBuffer();
                out.write(partialBuffer, 0, partialBuffer.length);
            }
            out.flush();
        }
        System.out.println("Download finished.");
    }

    public OkHttpClient getClient() {
        return okHttpClient;
    }

    /**
     * parse the filename of the url
     *
     * @param url url
     * @return filename
     */
    public static String parseFilename(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
