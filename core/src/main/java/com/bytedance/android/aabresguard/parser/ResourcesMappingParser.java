package com.bytedance.android.aabresguard.parser;

import com.bytedance.android.aabresguard.model.ResourcesMapping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ResourcesMappingParser {
    private static final Pattern MAP_DIR_PATTERN = Pattern.compile("\\s+(.*)->(.*)");
    private static final Pattern MAP_RES_PATTERN = Pattern.compile("\\s+(.*):(.*)->(.*)");
    private final Path mappingPath;

    public ResourcesMappingParser(Path mappingPath) {
        checkFileExistsAndReadable(mappingPath);
        this.mappingPath = mappingPath;
    }

    public ResourcesMapping parse() throws IOException {
        ResourcesMapping mapping = new ResourcesMapping();

        FileReader fr = new FileReader(mappingPath.toFile());
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null) {
            if (line.length() <= 0) {
                line = br.readLine();
                continue;
            }
            if (!line.contains(":")) {
                Matcher mat = MAP_DIR_PATTERN.matcher(line);
                if (mat.find()) {
                    String rawName = mat.group(1).trim();
                    String obfuscateName = mat.group(2).trim();
                    if (!line.contains("/") || line.contains(".")) {
                        throw new IllegalArgumentException("Unexpected resource dir: " + line);
                    }
                    mapping.putDirMapping(rawName, obfuscateName);
                }
            } else {
                Matcher mat = MAP_RES_PATTERN.matcher(line);
                if (mat.find()) {
                    String rawName = mat.group(2).trim();
                    String obfuscateName = mat.group(3).trim();
                    if (line.contains("/")) {
                        mapping.putEntryFileMapping(rawName, obfuscateName);
                    } else {
                        int packagePos = rawName.indexOf(".R.");
                        if (packagePos == -1) {
                            throw new IllegalArgumentException(String.format("the mapping file packageName is malformed, "
                                            + "it should be like com.bytedance.android.ugc.R.attr.test, yours %s\n",
                                    rawName
                            ));
                        }
                        mapping.putResourceMapping(rawName, obfuscateName);
                    }
                }
            }
            line = br.readLine();
        }

        br.close();
        fr.close();
        return mapping;
    }

}
