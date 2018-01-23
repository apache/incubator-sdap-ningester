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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SliceFileByDimension implements FileSlicer {

    private String sliceByDimension;
    private List<String> dimensions;
    private String dimensionNamePrefix;

    public void setDimensions(List<String> dims) {
        this.dimensions = dims;
    }

    public void setSliceByDimension(String sliceBy) {
        this.sliceByDimension = sliceBy;
    }

    public void setDimensionNamePrefix(String dimensionNamePrefix) {
        this.dimensionNamePrefix = dimensionNamePrefix;
    }

    public List<String> generateSlices(File inputfile) throws IOException {

        boolean isInteger = false;
        try {
            Integer.parseInt(this.sliceByDimension);
            isInteger = true;
        } catch (NumberFormatException e) {
            //ignore
        }
        return (isInteger) ? indexedDimensionSlicing(inputfile) : namedDimensionSlicing(inputfile);
    }

    List<String> indexedDimensionSlicing(File inputfile) throws IOException {

        // This is sort of a hack to help the python netcdf library. When you try to get dimensions by name and they are unnamed, the
        // python library uses 'phony_dim_' then the index of the dimension as the dimension name. Weird, I know.
        if (Strings.isNullOrEmpty(this.dimensionNamePrefix)) {
            this.dimensionNamePrefix = "phony_dim_";
        }
        Map<String, Integer> dimensionNameToLength;
        try (NetcdfDataset ds = NetcdfDataset.openDataset(inputfile.getAbsolutePath())) {


            // Because this is indexed-based dimension slicing, the dimensions are assumed to be unlimited with no names (ie. ds.dimensions == [])
            // Therefore, we need to find a 'representative' variable with dimensions that we can inspect and work with
            // 'lat' and 'lon' are common variable names in the datasets we work with. So try to find one of those first
            // Otherwise, just find the first variable that has the same number of dimensions as was given in this.dimensions
            List<String> commonVariableNames = Arrays.asList("lat", "latitude", "lon", "longitude");
            Optional<Variable> var = ds.getVariables().stream()
                    .filter(variable -> commonVariableNames.contains(variable.getShortName().toLowerCase())
                            || variable.getDimensions().size() == this.dimensions.size())
                    .findFirst();

            assert var.isPresent() : "Could not find a variable in " + inputfile.getName() + " with " + dimensions.size() + " dimension(s).";

            dimensionNameToLength = IntStream.range(0, this.dimensions.size()).boxed()
                    .collect(Collectors.toMap(dimIndex -> this.dimensionNamePrefix + dimIndex, dimIndex -> var.get().getDimension(dimIndex).getLength()));
        }

        return generateTileBoundrySlices(this.dimensionNamePrefix + this.sliceByDimension, dimensionNameToLength);

    }

    List<String> namedDimensionSlicing(File inputfile) throws IOException {
        Map<String, Integer> dimensionNameToLength;
        try (NetcdfDataset ds = NetcdfDataset.openDataset(inputfile.getAbsolutePath())) {

            dimensionNameToLength = ds.getDimensions().stream()
                    .filter(dimension -> this.dimensions.contains(dimension.getShortName()))
                    .collect(Collectors.toMap(Dimension::getShortName, Dimension::getLength));
        }

        return generateTileBoundrySlices(this.sliceByDimension, dimensionNameToLength);
    }

    List<String> generateTileBoundrySlices(String sliceByDimension, Map<String, Integer> dimensionNameToLength) {

        List<Set<String>> dimensionBounds = dimensionNameToLength.entrySet().stream()
                .map(stringIntegerEntry -> {
                    String dimensionName = stringIntegerEntry.getKey();
                    Integer lengthOfDimension = stringIntegerEntry.getValue();
                    Integer stepSize = (dimensionName.equals(sliceByDimension)) ? 1 : lengthOfDimension;

                    Set<String> bounds = new LinkedHashSet<>();
                    for (int i = 0; i < lengthOfDimension; i += stepSize) {
                        bounds.add(
                                dimensionName + ":" +
                                        i + ":" +
                                        (i + stepSize >= lengthOfDimension ? lengthOfDimension : i + stepSize));
                    }
                    return bounds;
                }).collect(Collectors.toList());

        return Sets.cartesianProduct(dimensionBounds)
                .stream()
                .map(tileSpecAsList -> tileSpecAsList.stream().collect(Collectors.joining(",")))
                .collect(Collectors.toList());
    }

}
