package com.example;

import lombok.Data;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * The small download task, download the part of the file
 */
@Data
public class PartialTask implements Callable<PartialRes> {

    /**
     * the id of this task
     */
    private final int partialId;
    /**
     * the beginning of file, inclusive
     */
    private final long beg;
    /**
     * the ending of file, exclusive
     */
    private final long end;
    /**
     * the parent file task
     */
    private final Task parentTask;

    /**
     *
     * @param parentTask the file task
     * @param beg the beginning index, inclusive
     * @param end the ending index, exclusive
     */
    public PartialTask(Task parentTask, int partialId, long beg, long end) {
        System.out.println("Task for " + parentTask.getTaskName() + " - " + partialId + ": " + beg + "-" + (end - 1));
        this.parentTask = parentTask;
        this.partialId = partialId;
        this.beg = beg;
        this.end = end;
    }

    @Override
    public PartialRes call() throws Exception {
        Request.Builder requestBuilder = new Request.Builder()
                .url(parentTask.getUrl())
                .header("Accept-Ranges", "bytes")
                .header("Range", "bytes=" + beg + "-" + (end - 1));

        /* if there has a version tag, use it */
        if (StringUtils.isNotBlank(parentTask.getEtag()))
            requestBuilder.header("If-Range", parentTask.getEtag());

        Request request = requestBuilder.build();

        OkHttpClient client = Downloader.getInstance().getClient();
        int len = (int) (end - beg);
        byte[] fileBuffer = new byte[len];
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();

            assert body != null && len == body.contentLength();

            byte[] buf = new byte[4096];
            ProgressBarBuilder progressBarBuilder = new ProgressBarBuilder()
                    .setUnit("kb", 1024)
                    .setStyle(ProgressBarStyle.ASCII)
                    .setTaskName("P-" + String.format("%04d", partialId))
                    .setUpdateIntervalMillis(500)
                    .setInitialMax(len);

            try (InputStream in = ProgressBar.wrap(body.byteStream(), progressBarBuilder)) {
                for (int tmp, idx = 0; (tmp = in.read(buf)) != -1; idx += tmp) {
                    try {
                        System.arraycopy(buf, 0, fileBuffer, idx, tmp);
                    } catch (Exception e) {
                        System.out.println("Part: " + partialId + ", len: " + len + ", idx: " + idx + ", tmp: " + tmp);
                        throw e;
                    }
                }
            }
        }
        return new PartialRes(fileBuffer);
    }
}
