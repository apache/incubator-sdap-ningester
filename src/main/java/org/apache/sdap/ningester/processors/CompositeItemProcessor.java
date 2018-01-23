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


package org.apache.sdap.ningester.processors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

public class CompositeItemProcessor<I, O> extends org.springframework.batch.item.support.CompositeItemProcessor<I, O> implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private List<String> processorBeanNames;

    public CompositeItemProcessor(List<String> processorBeanNames) {
        this.processorBeanNames = processorBeanNames;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        List<ItemProcessor<I, O>> delegates = new ArrayList<>();
        for (String processorBeanName : processorBeanNames) {
            delegates.add(applicationContext.getBean(processorBeanName, ItemProcessor.class));
        }

        setDelegates(delegates);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
