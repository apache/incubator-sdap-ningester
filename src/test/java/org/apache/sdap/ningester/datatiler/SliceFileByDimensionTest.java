/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sdap.ningester.datatiler;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SliceFileByDimensionTest {

    @Test
    public void testGenerateTileBoundrySlices() {

        SliceFileByDimension slicer = new SliceFileByDimension();
        slicer.setSliceByDimension("NUMROWS");

        Map<String, Integer> dimensionNameToLength = new LinkedHashMap<>();
        dimensionNameToLength.put("NUMROWS", 3163);
        dimensionNameToLength.put("NUMCELLS", 82);

        List<String> result = slicer.generateTileBoundrySlices("NUMROWS", dimensionNameToLength);

        assertThat(result.size(), is(3163));
        assertThat(result, hasItems("NUMROWS:0:1,NUMCELLS:0:82", "NUMROWS:1:2,NUMCELLS:0:82", "NUMROWS:3162:3163,NUMCELLS:0:82"));

    }

    @Test
    public void testGenerateTileBoundrySlices2() {

        SliceFileByDimension slicer = new SliceFileByDimension();
        slicer.setSliceByDimension("NUMROWS");

        Map<String, Integer> dimensionNameToLength = new LinkedHashMap<>();
        dimensionNameToLength.put("NUMROWS", 2);
        dimensionNameToLength.put("NUMCELLS", 82);

        List<String> result = slicer.generateTileBoundrySlices("NUMROWS", dimensionNameToLength);

        assertThat(result.size(), is(2));
        assertThat(result, hasItems("NUMROWS:0:1,NUMCELLS:0:82", "NUMROWS:1:2,NUMCELLS:0:82"));

    }

    @Test
    public void testGenerateSlicesByInteger() throws IOException {


        Integer expectedTiles = 1624;

        SliceFileByDimension slicer = new SliceFileByDimension();
        slicer.setDimensions(Arrays.asList("0", "1"));
        slicer.setSliceByDimension("1");

        Resource testResource = new ClassPathResource("granules/SMAP_L2B_SSS_04892_20160101T005507_R13080.h5");

        List<String> results = slicer.generateSlices(testResource.getFile());

        assertThat(results.size(), is(expectedTiles));

    }

    @Test
    public void testGenerateSlicesByName() throws IOException {


        Integer expectedTiles = 3163;

        SliceFileByDimension slicer = new SliceFileByDimension();
        slicer.setDimensions(Arrays.asList("NUMROWS", "NUMCELLS"));
        slicer.setSliceByDimension("NUMROWS");

        Resource testResource = new ClassPathResource("granules/ascat_20121029_010301_metopb_00588_eps_o_coa_2101_ovw.l2.nc");

        List<String> results = slicer.generateSlices(testResource.getFile());

        assertThat(results.size(), is(expectedTiles));

    }
}
