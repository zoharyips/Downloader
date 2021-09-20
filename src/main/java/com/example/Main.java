package com.example;


import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class Main {

    /**
     * --url=[url] --task=[n] --file=[filePath]
     *
     * @param args arguments
     */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (Objects.isNull(args) || args.length == 0)
            return;

        HashMap<String, String> optionMap = new HashMap<>();
        Option.optionList.forEach(option -> optionMap.put(option.getName(), null));

        /* read the options, is okay to read the unsupported options, we don't use them */
        for (int i = 0, splitIdx; i < args.length; i++) {
            if ((splitIdx = args[i].indexOf('=')) == -1) continue;
            optionMap.put(args[i].substring(2, splitIdx), args[i].substring(splitIdx + 1));
        }

        /* parse or check the options */
        String url = optionMap.get(Option.URL.getName());
        if (StringUtils.isBlank(url)) {
            System.err.println("option required: " + Option.URL.usage());
            return;
        }
        String filePath = optionMap.get(Option.FILE.getName());
        if (StringUtils.isBlank(filePath))
            filePath = System.getProperty("user.dir") + "/" + Downloader.parseFilename(url);
        else if (filePath.indexOf('/') == -1)
            filePath = System.getProperty("user.dir") + "/" + filePath;
        String task = StringUtils.defaultIfBlank(optionMap.get(Option.TASK.getName()), "8");

        /* put back the parsed options and show all */
        optionMap.put(Option.TASK.getName(), task);
        optionMap.put(Option.FILE.getName(), filePath);
        optionMap.forEach((option, value) -> System.out.println(option + ": " + value));

        /* start download task */
        Downloader.download(url, filePath, Integer.parseInt(task));
    }


    /**
     * some day you can create a help for every option by this class
     */
    @Getter
    enum Option {
        URL("url"),
        FILE("file"),
        TASK("task"),
        ;

        public static List<Option> optionList = new ArrayList<Option>() {
            {add(URL); add(FILE); add(TASK);}
        };

        private final String name;

        Option(String name) {
            this.name = name;
        }

        public String usage() {
            return "--" + name + "=" + "[" + name + "]";
        }
    }
}
