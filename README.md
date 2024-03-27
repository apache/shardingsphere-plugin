# Table of Contents

* [Table of Contents](#table-of-contents)
* [Overview](#overview)
* [Instruction](#instruction)
* [Plugin Implementation](#plugin-implementation)
    * [Feature Plugin](#feature-plugin)
        * [Encrypt Feature Plugin](#encrypt-feature-plugin)
            * [Like Encrypt Algorithm](#like-encrypt-algorithm)
            * [Standard Encrypt Algorithm](#standard-encrypt-algorithm)
        * [Sharding Feature Plugin](#sharding-feature-plugin)
            * [Sharding Algorithm](#sharding-algorithm)
    * [Infra Plugin](#infra-plugin)
        * [Infra Algorithm Plugin](#infra-algorithm-plugin)
            * [Distributed Key Generator](#distributed-key-generator)
            * [Message Digest Plugin](#message-digest-plugin)
        * [Connection Pool Plugin](#connection-pool-plugin)
        * [URL Loader Plugin](#url-loader-plugin)
    * [Kernel Plugin](#kernel-plugin)
        * [SQL Translator Plugin](#sql-translator-plugin)
    * [JDBC Adaptor Plugin](#jdbc-adaptor-plugin)
        * [JDBC Driver Config Plugin](#jdbc-driver-config-plugin)
    * [Mode Plugin](#mode-plugin)
        * [Mode Cluster Repository Plugin](#mode-cluster-repository-plugin)

# Overview

ShardingSphere plugin is designed to provide a plugin implementation for ShardingSphere pluggable architecture. You can refer to ShardingSphere [Dev Manual](https://shardingsphere.apache.org/document/current/en/dev-manual/) to extend the SPI.
Developers are welcome to contribute to the implementation of plugins and build a distributed database ecosystem of ShardingSphere.

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://github.com/apache/shardingsphere-plugin/blob/main/README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://github.com/apache/shardingsphere-plugin/blob/main/README_ZH.md)

# Instruction

These plugins can be found in [ShardingSphere plugin](https://github.com/apache/shardingsphere-plugin) repository. Plugins in ShardingSphere plugin repository would remain the same release plan with ShardingSphere.
When using ShardingSphere-JDBC, users only need to add maven dependencies to the project to complete the plugin installation. When using ShardingSphere-Proxy, they need to download the plugin jar package and the jar packages that the plugin may depend on, and then copy them to ShardingSphere-Proxy `ext-lib` directory.

When developers contribute new plugins, they need to refer to [Contributor Guide](https://shardingsphere.apache.org/community/en/involved/contribute/contributor/) and first execute `./mvnw clean install -DskipITs -DskipTests -Prelease` to package ShardingSphere basic SPI and test components, and then create a new module for plugin development.
Newly developed plugin code needs to follow ShardingSphere [Code of Conduct](https://shardingsphere.apache.org/community/en/involved/conduct/code/).

# Plugin Implementation

## Feature Plugin

### Encrypt Feature Plugin

#### Like Encrypt Algorithm

* CharDigestLike Encrypt Algorithm

Type：CHAR_DIGEST_LIKE

Attributes：

| *Name* | *DataType* | *Description*                                   |
|--------|------------|-------------------------------------------------|
| delta  | int        | Character Unicode offset（decimal number）        |
| mask   | int        | Character encryption mask（decimal number）       |
| start  | int        | Ciphertext Unicode initial code（decimal number） |
| dict   | String     | Common words                                    |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-like</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

#### Standard Encrypt Algorithm

* RC4 Encrypt Algorithm

Type: RC4

Attributes:

| *Name*        | *DataType* | *Description* |
|---------------|------------|---------------|
| rc4-key-value | String     | RC4 KEY       |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-rc4</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* SM3 Encrypt Algorithm

Type: SM3

Attributes:

| *Name*   | *DataType* | *Description*                              |
|----------|------------|--------------------------------------------|
| sm3-salt | String     | SM3 SALT (should be blank or 8 bytes long) |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-sm</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* SM4 Encrypt Algorithm

Type: SM4

Attributes:

| *Name*      | *DataType* | *Description*                                                            |
|-------------|------------|--------------------------------------------------------------------------|
| sm4-key     | String     | SM4 KEY (should be 16 bytes)                                             |
| sm4-mode    | String     | SM4 MODE (should be CBC or ECB)                                          |
| sm4-iv      | String     | SM4 IV (should be specified on CBC, 16 bytes long)                       |
| sm4-padding | String     | SM4 PADDING (should be PKCS5Padding or PKCS7Padding, NoPadding excepted) |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-sm</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### Sharding Feature Plugin

#### Sharding Algorithm

* Fixed interval sharding algorithm provided by CosId

A fixed time range sharding algorithm implemented by the tool class based on `me.ahoo.cosid:cosid-core`.
When the sharding key is a JSR-310 containing class or a time-related class, it will be converted to `java.time.LocalDateTime` before the next sharding.
See the discussion at https://github.com/apache/shardingsphere/issues/14047.

Type：COSID_INTERVAL

Attributes：

| *Name*                   | *DataType* | *Description*                                                                                                                                                           | *Default Value* |
|--------------------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| zone-id                  | String     | Time zone, which must follow the contained value of `java.time.ZoneId`. For example: Asia/Shanghai                                                                      |                 |
| logic-name-prefix        | String     | Prefix pattern of sharding data sources or tables                                                                                                                       |                 |
| datetime-lower           | String     | Datetime sharding lower boundary, pattern is consistent with the timestamp format of `yyyy-MM-dd HH:mm:ss`                                                              |                 |
| datetime-upper           | String     | Datetime sharding upper boundary, pattern is consistent with the timestamp format of `yyyy-MM-dd HH:mm:ss`                                                              |                 |
| sharding-suffix-pattern  | String     | Suffix pattern of sharding data sources or tables, must can be transformed to Java LocalDateTime, must be consistent with `datetime-interval-unit`. For example: yyyyMM |                 |
| datetime-interval-unit   | String     | Unit of sharding value interval, must can be transformed to Java ChronoUnit's Enum value. For example: MONTHS                                                           |                 |
| datetime-interval-amount | int        | Interval of sharding value, after which the next shard will be entered                                                                                                  |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* Snowflake key-based fixed interval sharding algorithm provided by CosId

Snowflake ID sharding algorithm with fixed time range implemented by tool class based on `me.ahoo.cosid:cosid-core`.
When the sharding key is a JSR-310 containing class or a time-related class, it will be converted to `java.time.LocalDateTime` before the next sharding.
See the discussion at https://github.com/apache/shardingsphere/issues/14047.

Type：COSID_INTERVAL_SNOWFLAKE

Attributes：

| *Name*                   | *DataType* | *Description*                                                                                                                                                           | *Default Value* |
|--------------------------|------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| zone-id                  | String     | Time zone, which must follow the contained value of `java.time.ZoneId`. For example: Asia/Shanghai                                                                      |                 |
| logic-name-prefix        | String     | Prefix pattern of sharding data sources or tables                                                                                                                       |                 |
| datetime-lower           | String     | Datetime sharding lower boundary, pattern is consistent with the timestamp format of `yyyy-MM-dd HH:mm:ss`                                                              |                 |
| datetime-upper           | String     | Datetime sharding upper boundary, pattern is consistent with the timestamp format of `yyyy-MM-dd HH:mm:ss`                                                              |                 |
| sharding-suffix-pattern  | String     | Suffix pattern of sharding data sources or tables, must can be transformed to Java LocalDateTime, must be consistent with `datetime-interval-unit`. For example: yyyyMM |                 |
| datetime-interval-unit   | String     | Unit of sharding value interval, must can be transformed to Java ChronoUnit's Enum value. For example: MONTHS                                                           |                 |
| datetime-interval-amount | int        | Interval of sharding value, after which the next shard will be entered                                                                                                  |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* Modulo sharding algorithm provided by CosId

Modulo sharding algorithm implemented by the tool class based on `me.ahoo.cosid:cosid-core`.
See the discussion at https://github.com/apache/shardingsphere/issues/14047 .

Type: COSID_MOD

Attributes:

| *Name*            | *DataType* | *Description*                                     |
|-------------------|------------|---------------------------------------------------|
| mod               | int        | Sharding count                                    |
| logic-name-prefix | String     | Prefix pattern of sharding data sources or tables |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## Infra Plugin

### Infra Algorithm Plugin

#### Distributed Key Generator

* Nano ID

Type:NANOID

Configurable Property:none

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-nanoid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* CosId

Type: COSID

Attributes：

| *Name*    | *DataType* | *Description*                                                                                                                                                                      | *Default Value* |
|-----------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| id-name   | String     | ID generator name                                                                                                                                                                  | `__share__`     |
| as-string | bool       | Whether to generate a string type ID: Convert `long` type ID to Base-62 `String` type (`Long.MAX_VALUE` maximum string length is 11 digits), and ensure the ordering of string IDs | `false`         |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* CosId-Snowflake

Type: COSID_SNOWFLAKE

Attributes：

| *Name*    | *DataType* | *Description*                                                                                                                                                                      | *Default Value* |
|-----------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| epoch     | String     | EPOCH of Snowflake ID Algorithm                                                                                                                                                    | `1477929600000` |
| as-string | bool       | Whether to generate a string type ID: Convert `long` type ID to Base-62 `String` type (`Long.MAX_VALUE` maximum string length is 11 digits), and ensure the ordering of string IDs | `false`         |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

#### Message Digest Plugin

* SM3

Type: SM3

Attributes：

| *Name*    | *DataType* | *Description*                                                                                                                                                                      | *Default Value* |
|-----------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| sm3-salt      | String    | SALT used by SM3 (null or 8 Bytes)| empty string  |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-message-digest-sm3</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### Connection Pool Plugin

ShardingSphere-Proxy supports common data source connection pools: HikariCP, C3P0, DBCP.

The connection pool can be specified through the parameter `dataSourceClassName`. When not specified, the default data source connection pool is HikariCP.

* C3P0 Connection Pool

Sample:

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
    url: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
```

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-data-source-pool-c3p0</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* DBCP Connection Pool

Sample:

```yaml
dataSources:
  ds_0:
    dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
    url: jdbc:mysql://localhost:3306/ds_3
    username: root
    password:
```

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-data-source-pool-dbcp</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### URL Loader Plugin

Shardingsphere-JDBC supports user-defined URL loader plugin, allowing users to load YAML configuration during the initial startup phase through custom methods. For example, YAML configuration information can be stored using relative paths, absolute paths, Apollo. 

* Apollo URL Loader

Type: Apollo

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-url-apollo</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

Using Apollo to store JDBC YAML configuration, as shown below:
```yaml
# Declare the use of Apollo as the storage method for configuration in the ShardingSphere-JDBC driver URL, and specify the Apollo Namespace storing the YAML as `test_namespace`.
jdbc:shardingsphere:apollo:test_namespace
```

## Kernel Plugin

### SQL Translator Plugin

* JooQ SQL translator

Type: JOOQ

Attributes:

None

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-kernel-sql-translator-jooq</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## JDBC Adaptor Plugin

### JDBC Driver Config Plugin

ShardingSphere-JDBC provides a JDBC Driver, which can be used only through configuration changes without rewriting the code.

* Apollo Driver Config

Load JDBC URL of the yaml configuration file in the specified namespace of apollo:
```
jdbc:shardingsphere:apollo:TEST.test_namespace
```

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-jdbc-driver-apollo</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## Mode Plugin

### Mode Cluster Repository Plugin

* Nacos Repository

Type: Nacos

Mode: Cluster

Attributes:

| *Name*                    | *Type* | *Description*                                     | *Default Value* |
|---------------------------|--------|---------------------------------------------------|-----------------|
| clusterIp                 | String | Unique identifier in cluster                      | Host IP         |
| retryIntervalMilliseconds | long   | Milliseconds of retry interval                    | 500             |
| maxRetries                | int    | Max retries for client to check data availability | 3               |
| timeToLiveSeconds         | int    | Seconds of ephemeral instance live                | 30              |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-mode-cluster-repository-nacos</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* Consul Repository

Due to the limitation of the Maven module of `com.ecwid.consul:consul-api:1.4.5`, users cannot connect to the Consul Agent through the gRPC port.

The `serverLists` property of the `Consul` implementation is by design and can only be connected to a single Consul Agent via an HTTP endpoint.
`serverLists` uses relaxed URL matching principles.
1. When `serverLists` is empty, it will be resolved to the Consul Agent instance of `http://127.0.0.1:8500`.
2. When `serverLists` is `hostName`, it will be resolved to the Consul Agent instance of `http://hostName:8500`.
3. When `serverLists` is `hostName:port`, it will be resolved to the Consul Agent instance of `http://hostName:port`.
4. When `serverLists` is `http://hostName:port`, it will be resolved to the Consul Agent instance of `http://hostName:port`.
5. When `serverLists` is `https://hostName:port`, it will be resolved to the Consul Agent instance of `https://hostName:port`.

Type: Consul

Mode: Cluster

Attributes:

| *Name*                  | *Type* | *Description*                      | *Default Value* |
|-------------------------|--------|------------------------------------|-----------------|
| timeToLiveSeconds       | String | Seconds of ephemeral instance live | 30s             |
| blockQueryTimeToSeconds | long   | Seconds of query timeout           | 60              |

Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-mode-cluster-repository-consul</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```
