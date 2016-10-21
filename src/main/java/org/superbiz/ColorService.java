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

import javax.ejb.Lock;
import javax.ejb.Singleton;
import javax.swing.text.html.StyleSheet;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;

@Lock(READ)
@Singleton
@Path("/color")
public class ColorService {

    private String color;

    public static void main(String[] args) {
        ColorService colorService = new ColorService();
        colorService.getColor();
        colorService.setColor(args[1]);
        colorService.getColorObject();
        colorService.computeEmotion(args[1], args[2], args[3]);
    }

    public ColorService() {
        this.color = "white";
    }

    @GET
    public String getColor() {
        return color;
    }

    @Lock(WRITE)
    @Path("{color}")
    @POST
    public void setColor(@PathParam("color") String color) {
        this.color = color;
    }

    @Path("object")
    @GET
    @Produces({APPLICATION_JSON})
    public Color getColorObject() {
        StyleSheet s = new StyleSheet();
        java.awt.Color c = s.stringToColor(color);
        return new Color(color, c.getRed(), c.getGreen(), c.getBlue());
    }

    @Path("compute-emotion")
    @GET
    public String computeEmotion(@QueryParam("op") String op, @QueryParam("c1") String color1, @QueryParam("c2") String color2) {
        IntBinaryOperator operator = map.get(op);
        RGB rgb1 = new RGB(color1);
        RGB rgb2 = new RGB(color2);
        RGB rgb3 = new RGB(
            operator.applyAsInt(rgb1.red, rgb2.red),
            operator.applyAsInt(rgb1.blue, rgb2.blue),
            operator.applyAsInt(rgb1.green, rgb2.green));
        return emotion[(rgb3.red + rgb3.blue + rgb3.green) % emotion.length];
    }

    private Map<String, IntBinaryOperator> map = new HashMap<String, IntBinaryOperator>() {{
        put("add", (a, b) -> a + b);
        put("sub", (a, b) -> a - b);
        put("mul", (a, b) -> a * b);
        put("div", (a, b) -> a / b);
    }};

    private class RGB {
        Byte red;
        Byte blue;
        Byte green;
        RGB(String color) {
            red = Byte.parseByte(color.substring(0, 2), 16);
            blue = Byte.parseByte(color.substring(2, 4), 16);
            green = Byte.parseByte(color.substring(4, 6), 16);
        }
        RGB(Integer red, Integer blue, Integer green) {
            this.red = red.byteValue();
            this.blue = blue.byteValue();
            this.green = green.byteValue();
        }
    }

    private String emotion[] = {"love", "compassion", "hope", "willpower", "fear", "greed", "rage"};
}
