package com.bytedance.android.aabresguard.parser;

import com.bytedance.android.aabresguard.model.xml.AabResGuardConfig;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.nio.file.Path;
import java.util.Iterator;

import static com.android.tools.build.bundletool.model.utils.files.FilePreconditions.checkFileExistsAndReadable;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AabResGuardXmlParser {
    private final Path configPath;

    public AabResGuardXmlParser(Path configPath) {
        checkFileExistsAndReadable(configPath);
        this.configPath = configPath;
    }

    public AabResGuardConfig parse() throws DocumentException {
        AabResGuardConfig aabResGuardConfig = new AabResGuardConfig();
        SAXReader reader = new SAXReader();
        Document doc = reader.read(configPath.toFile());
        Element root = doc.getRootElement();
        for (Iterator i = root.elementIterator("issue"); i.hasNext(); ) {
            Element element = (Element) i.next();
            String id = element.attributeValue("id");
            if (id == null || !id.equals("whitelist")) {
                continue;
            }
            String isActive = element.attributeValue("isactive");
            if (isActive != null && isActive.equals("true")) {
                aabResGuardConfig.setUseWhiteList(true);
            }
            for (Iterator rules = element.elementIterator("path"); rules.hasNext(); ) {
                Element ruleElement = (Element) rules.next();
                String rule = ruleElement.attributeValue("value");
                if (rule != null) {
                    aabResGuardConfig.addWhiteList(rule);
                }
            }
        }

        // file filter
        aabResGuardConfig.setFileFilter(new FileFilterXmlParser(configPath).parse());

        // string filter
        aabResGuardConfig.setStringFilterConfig(new StringFilterXmlParser(configPath).parse());

        return aabResGuardConfig;
    }
}
