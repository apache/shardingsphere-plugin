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

package org.apache.shardingsphere.infra.algorithm.keygen.cosid;

import me.ahoo.cosid.IdGenerator;
import me.ahoo.cosid.StringIdGeneratorDecorator;
import me.ahoo.cosid.converter.PrefixIdConverter;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.provider.DefaultIdGeneratorProvider;
import me.ahoo.cosid.provider.NotFoundIdGeneratorException;
import me.ahoo.cosid.segment.DefaultSegmentId;
import me.ahoo.cosid.segment.IdSegmentDistributor;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.algorithm.keygen.cosid.constant.CosIdKeyGenerateConstants;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CosIdKeyGenerateAlgorithmTest {

    @Test
    void assertGenerateKey() {
        String idName = "test-cosid";
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.set(idName, defaultSegmentId);
        Properties props = new Properties();
        props.setProperty(CosIdKeyGenerateConstants.ID_NAME_KEY, idName);
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID", props);
        assertThat(algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next(), is(1L));
        assertThat(algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next(), is(2L));
    }

    @Test
    void assertGenerateKeyWhenNotSetIdName() {
        DefaultSegmentId defaultSegmentId = new DefaultSegmentId(new IdSegmentDistributor.Mock());
        DefaultIdGeneratorProvider.INSTANCE.setShare(defaultSegmentId);
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID");
        assertThat(algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next(), is(1L));
        assertThat(algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next(), is(2L));
    }

    @Test
    void assertGenerateKeyWhenIdProviderIsEmpty() {
        DefaultIdGeneratorProvider.INSTANCE.clear();
        assertThrows(NotFoundIdGeneratorException.class, () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID").generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next());
    }

    @Disabled
    // TODO fix this test, IdGenerator name:[__share__] not found.
    @Test
    void assertGenerateKeyAsString() {
        String idName = "test-cosid-as-string";
        String prefix = "test_";
        IdGenerator idGeneratorDecorator = new StringIdGeneratorDecorator(new MillisecondSnowflakeId(1, 0), new PrefixIdConverter(prefix, Radix62IdConverter.INSTANCE));
        DefaultIdGeneratorProvider.INSTANCE.set(idName, idGeneratorDecorator);
        Properties props = new Properties();
        props.setProperty("as-string", Boolean.TRUE.toString());
        KeyGenerateAlgorithm algorithm = TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID", props);
        Comparable<?> actual = algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
        assertThat(actual, instanceOf(String.class));
        assertThat(actual.toString(), startsWith(prefix));
        assertThat(actual.toString().length(), lessThanOrEqualTo(16));
    }
}
