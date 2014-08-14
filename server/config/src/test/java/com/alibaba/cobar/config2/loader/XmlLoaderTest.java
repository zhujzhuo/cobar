package com.alibaba.cobar.config2.loader;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.cobar.config2.CobarConfigModel;

/**
 * @author xianmao.hexm
 */
public class XmlLoaderTest extends TestCase {

    public void testPerformance() throws Exception {
        XmlLoader loader = new XmlLoader();
        CobarConfigModel config = null;
        for (int i = 0; i < 100; i++) {
            config = loader.load("/server.xml");
        }
        Assert.assertNotNull(config);
    }

}
