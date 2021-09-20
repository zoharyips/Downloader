package com.example;

import lombok.Data;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

/**
 * the main task for download the url
 */
@Data
public class Task {

    private final String taskName;
    private final String url;
    private final String etag;
    private final long len;
    private final long avg;

    public static final int DEFAULT_TASK_COUNT = 8;

    public Task(String url, int partitions) throws IOException {
        this.url = url;
        this.taskName = Downloader.parseFilename(url);

        Request request = new Request.Builder().url(url).head().build();
        Response headResponse = Downloader.getInstance().getClient().newCall(request).execute();

        this.etag = headResponse.header("etag");

        len = Integer.parseInt(Objects.requireNonNull(headResponse.header("content-length")));
        avg = len / partitions;
        System.out.println("partitions: " + partitions + "\nlen: " + len + "\navg: " + avg);
    }
}
