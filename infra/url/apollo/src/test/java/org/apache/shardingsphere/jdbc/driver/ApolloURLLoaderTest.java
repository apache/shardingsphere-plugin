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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.jdbc.driver;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ApolloURLLoaderTest {
    
    private final ApolloURLLoader loader = new ApolloURLLoader();
    
    @Test
    void assertLoadYAMLContent() {
        ConfigFile config = Mockito.mock(ConfigFile.class, RETURNS_DEEP_STUBS);
        try (MockedStatic<ConfigService> configService = mockStatic(ConfigService.class)) {
            configService.when(() -> ConfigService.getConfigFile("TEST1.test_namespace", ConfigFileFormat.YAML)).thenReturn(config);
            String expectedConfig = "Mocked yaml config" + System.lineSeparator();
            when(config.getContent()).thenReturn(expectedConfig);
            assertThat(loader.load("TEST1.test_namespace", new Properties()), is(expectedConfig));
            assertThat(loader.load("TEST1.test_namespace", PropertiesBuilder.build(new Property("configFileFormat", "yaml"))), is(expectedConfig));
        }
    }
}
