package com.bytedance.android.aabresguard.parser;

import com.bytedance.android.aabresguard.BaseTest;
import com.bytedance.android.aabresguard.model.xml.AabResGuardConfig;

import org.dom4j.DocumentException;
import org.junit.Test;

/**
 * Created by YangJing on 2019/10/14 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class AabResGuardXmlParserTest extends BaseTest {

    @Test
    public void test() throws DocumentException {
        AabResGuardXmlParser parser = new AabResGuardXmlParser(loadResourceFile("demo/config.xml").toPath());
        AabResGuardConfig config = parser.parse();
        assert config != null;
        assert config.isUseWhiteList();
        assert config.getFileFilter() != null;
        assert config.getFileFilter().getRules().size() == 2;
        assert config.getWhiteList().size() == 1;
    }
}
