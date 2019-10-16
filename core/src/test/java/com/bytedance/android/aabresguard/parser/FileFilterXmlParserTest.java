package com.bytedance.android.aabresguard.parser;

import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.model.xml.FileFilterConfig;

import org.dom4j.DocumentException;
import org.junit.Test;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class FileFilterXmlParserTest extends BaseTest {

    @Test
    public void test() throws DocumentException {
        FileFilterXmlParser parser = new FileFilterXmlParser(loadResourceFile("demo/config-filter.xml").toPath());
        FileFilterConfig fileFilter = parser.parse();
        assert fileFilter != null;
        assert fileFilter.isActive();
        assert fileFilter.getRules().size() == 2;
    }
}
