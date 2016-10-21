/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz;

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Arquillian will start the container, deploy all @Deployment bundles, then run all the @Test methods.
 *
 * A strong value-add for Arquillian is that the test is abstracted from the server.
 * It is possible to rerun the same test against multiple adapters or server configurations.
 *
 * A second value-add is it is possible to build WebArchives that are slim and trim and therefore
 * isolate the functionality being tested.  This also makes it easier to swap out one implementation
 * of a class for another allowing for easy mocking.
 *
 */
@RunWith(Arquillian.class)
public class ColorServiceTest extends Assert {

    /**
     * ShrinkWrap is used to create a war file on the fly.
     *
     * The API is quite expressive and can build any possible
     * flavor of war file.  It can quite easily return a rebuilt
     * war file as well.
     *
     * More than one @Deployment method is allowed.
     */
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class).addClasses(ColorService.class, Color.class);
    }

    /**
     * This URL will contain the following URL data
     *
     *  - http://<host>:<port>/<webapp>/
     *
     * This allows the test itself to be agnostic of server information or even
     * the name of the webapp
     *
     */
    @ArquillianResource
    private URL webappUrl;


    @Test
    public void postAndGet() throws Exception {

        // POST
        {
            final WebClient webClient = WebClient.create(webappUrl.toURI());
            final Response response = webClient.path("color/green").post(null);

            assertEquals(204, response.getStatus());
        }

        // GET
        {
            final WebClient webClient = WebClient.create(webappUrl.toURI());
            final Response response = webClient.path("color").get();

            assertEquals(200, response.getStatus());

            final String content = slurp((InputStream) response.getEntity());

            assertEquals("green", content);
        }

    }

    @Test
    public void getColorObject() throws Exception {

        final WebClient webClient = WebClient.create(webappUrl.toURI());
        webClient.accept(MediaType.APPLICATION_JSON);

        final Color color = webClient.path("color/object").get(Color.class);

        assertNotNull(color);
        assertEquals("white", color.getName());
        assertEquals(0xff, color.getR());
        assertEquals(0xff, color.getG());
        assertEquals(0xff, color.getB());
    }

    @Test
    public void testMapOfBinaryOperations() {
        class TestVector {
            String op;
            String color1;
            String color2;
            String emotion;
            TestVector(String op, String color1, String color2, String emotion) {
                this.op = op;
                this.color1 = color1;
                this.color2 = color2;
                this.emotion = emotion;
            }
        }
        TestVector[] testVectors = new TestVector[] {
            new TestVector("add", "0a0a0a", "050505", "willpower"),
            new TestVector("sub", "0a0a0a", "080808", "rage"),
            new TestVector("mul", "030303", "010101", "hope"),
            new TestVector("div", "080808", "020202", "greed")
        };
        ColorService colorService = new ColorService();
        for (TestVector test : testVectors) {
            assertEquals(test.emotion, colorService.computeEmotion(test.op, test.color1, test.color2));
        }
    }

    /**
     * Reusable utility method
     * Move to a shared class or replace with equivalent
     */
    public static String slurp(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
        return new String(out.toByteArray());
    }
}
