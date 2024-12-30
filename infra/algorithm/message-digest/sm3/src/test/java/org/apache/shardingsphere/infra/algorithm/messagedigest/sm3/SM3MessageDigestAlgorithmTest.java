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

package org.apache.shardingsphere.infra.algorithm.messagedigest.sm3;

import org.apache.shardingsphere.infra.algorithm.messagedigest.core.MessageDigestAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class SM3MessageDigestAlgorithmTest {

    private MessageDigestAlgorithm digestAlgorithm;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put("sm3-salt", "test1234");
        digestAlgorithm = TypedSPILoader.getService(MessageDigestAlgorithm.class, "SM3", props);
    }

    @Test
    void assertDigest() {
        Object actual = digestAlgorithm.digest("test1234");
        assertThat(actual, is("9587fe084ee4b53fe629c6ae5519ee4d55def8ed4badc8588d3be9b99bd84aba"));
    }

    @Test
    void assertDigestWithoutSalt() {
        digestAlgorithm.init(new Properties());
        assertThat(digestAlgorithm.digest("test1234"), is("ab847c6f2f6a53be88808c5221bd6ee0762e1af1def82b21d2061599b6cf5c79"));
    }

    @Test
    void assertDigestWithNullPlaintext() {
        assertNull(digestAlgorithm.digest(null));
    }
}
