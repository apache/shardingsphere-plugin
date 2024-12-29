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
import me.ahoo.cosid.snowflake.ClockSyncSnowflakeId;
import me.ahoo.cosid.snowflake.MillisecondSnowflakeId;
import me.ahoo.cosid.snowflake.SnowflakeId;
import me.ahoo.cosid.snowflake.StringSnowflakeId;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.algorithm.keygen.cosid.constant.CosIdKeyGenerateConstants;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContextAware;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CosId snowflake key generate algorithm.
 */
public final class CosIdSnowflakeKeyGenerateAlgorithm implements KeyGenerateAlgorithm, ComputeNodeInstanceContextAware {
    
    public static final long DEFAULT_EPOCH;
    
    public static final String AS_STRING_KEY = "as-string";
    
    public static final String EPOCH_KEY = "epoch";
    
    private Properties props;
    
    private SnowflakeId snowflakeId;
    
    private boolean asString;
    
    private long epoch;
    
    static {
        DEFAULT_EPOCH = LocalDateTime.of(2016, 11, 1, 0, 0, 0).toInstant(ZoneId.systemDefault().getRules().getOffset(Instant.now())).toEpochMilli();
    }
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        asString = getAsString(props);
        epoch = getEpoch(props);
    }
    
    private boolean getAsString(final Properties props) {
        return Boolean.parseBoolean(props.getProperty(AS_STRING_KEY, Boolean.FALSE.toString()));
    }
    
    private long getEpoch(final Properties props) {
        long result = Long.parseLong(props.getProperty(EPOCH_KEY, String.valueOf(DEFAULT_EPOCH)));
        ShardingSpherePreconditions.checkState(result > 0L, () -> new AlgorithmInitializationException(this, "Epoch must be positive."));
        return result;
    }
    
    @Override
    public void setComputeNodeInstanceContext(final ComputeNodeInstanceContext instanceContext) {
        int workerId = instanceContext.generateWorkerId(props);
        MillisecondSnowflakeId millisecondSnowflakeId =
                new MillisecondSnowflakeId(epoch, MillisecondSnowflakeId.DEFAULT_TIMESTAMP_BIT, MillisecondSnowflakeId.DEFAULT_MACHINE_BIT, MillisecondSnowflakeId.DEFAULT_SEQUENCE_BIT, workerId);
        snowflakeId = new StringSnowflakeId(new ClockSyncSnowflakeId(millisecondSnowflakeId), Radix62IdConverter.PAD_START);
    }
    
    @Override
    public Collection<Comparable<?>> generateKeys(final AlgorithmSQLContext algorithmSQLContext, final int keyGenerateCount) {
        return IntStream.range(0, keyGenerateCount).mapToObj(each -> generateKey()).collect(Collectors.toList());
    }
    
    private Comparable<?> generateKey() {
        if (asString) {
            return getSnowflakeId().generateAsString();
        }
        return getSnowflakeId().generate();
    }
    
    private SnowflakeId getSnowflakeId() {
        ShardingSpherePreconditions.checkNotNull(snowflakeId, () -> new AlgorithmInitializationException(this, "Instance context not set yet."));
        return snowflakeId;
    }
    
    @Override
    public String getType() {
        return CosIdKeyGenerateConstants.TYPE_PREFIX + "SNOWFLAKE";
    }
}
