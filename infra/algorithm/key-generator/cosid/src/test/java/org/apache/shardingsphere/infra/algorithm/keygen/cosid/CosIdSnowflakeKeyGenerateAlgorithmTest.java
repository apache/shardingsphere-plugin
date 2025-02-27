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

import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeIdStateParser;
import me.ahoo.cosid.snowflake.SnowflakeIdState;
import me.ahoo.cosid.snowflake.SnowflakeIdStateParser;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CosIdSnowflakeKeyGenerateAlgorithmTest {
    
    private static final int FIXTURE_WORKER_ID = 0;
    
    private final SnowflakeIdStateParser snowflakeIdStateParser = new MillisecondSnowflakeIdStateParser(
            CosIdSnowflakeKeyGenerateAlgorithm.DEFAULT_EPOCH,
            MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT,
            MillisecondSnowflakeId.DEFAULT_MACHINE_BIT,
            MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT);
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    void assertGenerateKey() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE");
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), eventBusContext);
        instanceContext.init(mock(WorkerIdGenerator.class), mock(LockContext.class));
        algorithm.setComputeNodeInstanceContext(instanceContext);
        long firstActualKey = (Long) algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
        long secondActualKey = (Long) algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
        SnowflakeIdState firstActualState = snowflakeIdStateParser.parse(firstActualKey);
        SnowflakeIdState secondActualState = snowflakeIdStateParser.parse(secondActualKey);
        assertThat(firstActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(firstActualState.getSequence(), is(1L));
        assertThat(secondActualState.getMachineId(), is(FIXTURE_WORKER_ID));
        long expectedSecondSequence = 2L;
        assertThat(secondActualState.getSequence(), is(expectedSecondSequence));
    }
    
    @Test
    void assertGenerateKeyModUniformity() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE");
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), eventBusContext);
        instanceContext.init(mock(WorkerIdGenerator.class), mock(LockContext.class));
        algorithm.setComputeNodeInstanceContext(instanceContext);
        int divisor = 4;
        int total = 99999;
        int avg = total / divisor;
        double tolerance = avg * .0015;
        int mod0Counter = 0;
        int mod1Counter = 0;
        int mod2Counter = 0;
        int mod3Counter = 0;
        for (int i = 0; i < total; i++) {
            long id = (Long) algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
            int mod = (int) (id % divisor);
            switch (mod) {
                case 0:
                    mod0Counter++;
                    break;
                case 1:
                    mod1Counter++;
                    break;
                case 2:
                    mod2Counter++;
                    break;
                case 3:
                    mod3Counter++;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + mod);
            }
            int wait = ThreadLocalRandom.current().nextInt(10, 1000);
            LockSupport.parkNanos(wait);
        }
        assertThat((double) mod0Counter, closeTo(avg, tolerance));
        assertThat((double) mod1Counter, closeTo(avg, tolerance));
        assertThat((double) mod2Counter, closeTo(avg, tolerance));
        assertThat((double) mod3Counter, closeTo(avg, tolerance));
    }
    
    @Test
    void assertGenerateKeyAsString() {
        Properties props = new Properties();
        props.setProperty(CosIdSnowflakeKeyGenerateAlgorithm.AS_STRING_KEY, Boolean.TRUE.toString());
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE", props);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), eventBusContext);
        instanceContext.init(mock(WorkerIdGenerator.class), mock(LockContext.class));
        algorithm.setComputeNodeInstanceContext(instanceContext);
        Comparable<?> actualKey = algorithm.generateKeys(mock(AlgorithmSQLContext.class), 1).iterator().next();
        assertThat(actualKey, instanceOf(String.class));
        String actualStringKey = (String) actualKey;
        assertThat(actualStringKey.length(), is(Radix62IdConverter.MAX_CHAR_SIZE));
        long actualLongKey = Radix62IdConverter.PAD_START.asLong(actualStringKey);
        SnowflakeIdState actualState = snowflakeIdStateParser.parse(actualLongKey);
        assertThat(actualState.getMachineId(), is(FIXTURE_WORKER_ID));
        assertThat(actualState.getSequence(), is(1L));
    }
    
    @Test
    void assertGenerateKeyWhenNoneInstanceContext() {
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE").generateKeys(mock(AlgorithmSQLContext.class), 1));
    }
    
    @Test
    void assertGenerateKeyWhenNegative() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE");
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        when(workerIdGenerator.generate(new Properties())).thenReturn(-1);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), eventBusContext);
        instanceContext.init(workerIdGenerator, mock(LockContext.class));
        assertThrows(IllegalArgumentException.class, () -> algorithm.setComputeNodeInstanceContext(instanceContext));
    }
    
    @Test
    void assertGenerateKeyWhenGreaterThen1023() {
        CosIdSnowflakeKeyGenerateAlgorithm algorithm = (CosIdSnowflakeKeyGenerateAlgorithm) TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE");
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        when(workerIdGenerator.generate(new Properties())).thenReturn(1024);
        ComputeNodeInstanceContext instanceContext = new ComputeNodeInstanceContext(new ComputeNodeInstance(mock(InstanceMetaData.class)), new ModeConfiguration("Standalone", null), eventBusContext);
        instanceContext.init(workerIdGenerator, mock(LockContext.class));
        assertThrows(IllegalArgumentException.class, () -> algorithm.setComputeNodeInstanceContext(instanceContext));
    }
    
    @Test
    void assertEpochWhenOutOfRange() {
        Properties props = new Properties();
        props.setProperty("epoch", "0");
        assertThrows(AlgorithmInitializationException.class,
                () -> TypedSPILoader.getService(KeyGenerateAlgorithm.class, "COSID_SNOWFLAKE", props).generateKeys(mock(AlgorithmSQLContext.class), 1));
    }
}
