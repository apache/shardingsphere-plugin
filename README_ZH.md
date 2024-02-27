# 目录

* [目录](#目录)
* [概述](#概述)
* [说明](#说明)
* [插件实现](#插件实现)
    * [功能插件](#功能插件)
        * [加密功能插件](#加密功能插件)
            * [模糊加密算法插件](#模糊加密算法插件)
            * [标准加密算法插件](#标准加密算法插件)
        * [分片功能插件](#分片功能插件)
            * [分片算法插件](#分片算法插件)
    * [基础设施插件](#基础设施插件)
        * [基础算法插件](#基础算法插件)
            * [分布式主键生成插件](#分布式主键生成插件)
        * [连接池插件](#连接池插件)
    * [内核插件](#内核插件)
        * [SQL 翻译器插件](#sql-翻译器插件)
    * [JDBC 接入端插件](#jdbc-接入端插件)
        * [JDBC Driver 配置插件](#jdbc-driver-配置插件)
    * [治理中心插件](#治理中心插件)
        * [治理中心集群模式持久化插件](#治理中心集群模式持久化插件)

# 概述

ShardingSphere plugin 旨在为 ShardingSphere 可插拔架构提供插件实现，可以参考 ShardingSphere [开发者手册](https://shardingsphere.apache.org/document/current/cn/dev-manual/)对 SPI 进行扩展。
欢迎广大开发者们积极贡献插件实现，一起打造 ShardingSphere 分布式的数据库生态系统。

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](https://github.com/apache/shardingsphere-plugin/blob/main/README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](https://github.com/apache/shardingsphere-plugin/blob/main/README_ZH.md)

# 说明

这些插件可以在 [ShardingSphere plugin](https://github.com/apache/shardingsphere-plugin) 仓库中找到，ShardingSphere plugin 仓库中的插件会和 ShardingSphere 保持相同的发布节奏。
用户使用 ShardingSphere-JDBC 时，只需要将 maven 依赖添加到项目中即可完成插件安装，使用 ShardingSphere-Proxy 时，需要下载插件 jar 包及插件可能依赖的 jar 包，然后拷贝到 ShardingSphere-Proxy 的 `ext-lib` 目录下。

开发者贡献新的插件时，需要参考[贡献者指南](https://shardingsphere.apache.org/community/cn/involved/contribute/contributor/)，先执行 `./mvnw clean install -DskipITs -DskipTests -Prelease` 打包 ShardingSphere 基础 SPI 及测试组件，然后再新建模块进行插件开发。
新开发的插件代码需要遵循 ShardingSphere [开发规范](https://shardingsphere.apache.org/community/cn/involved/conduct/code/)。

# 插件实现

## 功能插件

### 加密功能插件

#### 模糊加密算法插件

* 单字符摘要模糊加密算法

类型：CHAR_DIGEST_LIKE

可配置属性：

| *名称*  | *数据类型* | *说明*               |
|-------|--------|--------------------|
| delta | int    | 字符Unicode码偏移量（十进制） |
| mask  | int    | 字符加密掩码（十进制）        |
| start | int    | 密文Unicode初始码（十进制）  |
| dict  | String | 常见字                |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-like</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

#### 标准加密算法插件

* RC4 加密算法

类型：RC4

可配置属性：

| *名称*          | *数据类型* | *说明*        |
|---------------|--------|-------------|
| rc4-key-value | String | RC4 使用的 KEY |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-rc4</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* SM3 加密算法

类型：SM3

可配置属性：

| 名称        | 数据类型 | 说明         |
| ------------- | --------- | ------------- |
| sm3-salt      | String    | SM3 使用的 SALT（空 或 8 Bytes） |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-sm</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* SM4 加密算法

类型：SM4

可配置属性：

| 名称         | 数据类型 | 说明         |
| ------------- | --------- | ------------- |
| sm4-key       | String    | SM4 使用的 KEY （16 Bytes） |
| sm4-mode      | String    | SM4 使用的 MODE （CBC 或 ECB） |
| sm4-iv        | String    | SM4 使用的 IV （MODE为CBC时需指定，16 Bytes）|
| sm4-padding   | String    | SM4 使用的 PADDING （PKCS5Padding 或 PKCS7Padding，暂不支持NoPadding）|

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-encrypt-sm</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### 分片功能插件

#### 分片算法插件

* 基于 CosId 的固定时间范围的分片算法

基于 `me.ahoo.cosid:cosid-core` 的工具类实现的固定时间范围的分片算法。
当分片键为 JSR-310 的所含类或与时间相关的类，将转换为`java.time.LocalDateTime`后再做下一步分片。
参考 https://github.com/apache/shardingsphere/issues/14047 的讨论。

类型：COSID_INTERVAL

可配置属性：

| *属性名称*                   | *数据类型* | *说明*                                                                                       | *默认值* |
|--------------------------|--------|--------------------------------------------------------------------------------------------|-------|
| zone-id                  | String | 时区，必须遵循 `java.time.ZoneId` 的所含值。 例如：Asia/Shanghai                                          |       |
| logic-name-prefix        | String | 分片数据源或真实表的前缀格式                                                                             |       |
| datetime-lower           | String | 时间分片下界值，格式与 `yyyy-MM-dd HH:mm:ss` 的时间戳格式一致                                                 |       |
| datetime-upper           | String | 时间分片上界值，格式与 `yyyy-MM-dd HH:mm:ss` 的时间戳格式一致                                                 |       |
| sharding-suffix-pattern  | String | 分片数据源或真实表的后缀格式，必须遵循 Java DateTimeFormatter 的格式，必须和 `datetime-interval-unit` 保持一致。例如：yyyyMM |       |
| datetime-interval-unit   | String | 分片键时间间隔单位，必须遵循 Java ChronoUnit 的枚举值。例如：MONTHS                                              |       |
| datetime-interval-amount | int    | 分片键时间间隔，超过该时间间隔将进入下一分片                                                                     |       |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* 基于 CosId 的雪花ID固定时间范围的分片算法

基于 `me.ahoo.cosid:cosid-core` 的工具类实现的雪花ID固定时间范围的分片算法。
当分片键为 JSR-310 的所含类或与时间相关的类，将转换为`java.time.LocalDateTime`后再做下一步分片。
参考 https://github.com/apache/shardingsphere/issues/14047 的讨论。

类型：COSID_INTERVAL_SNOWFLAKE

可配置属性：

| *属性名称*                   | *数据类型* | *说明*                                                                                       | *默认值* |
|--------------------------|--------|--------------------------------------------------------------------------------------------|-------|
| zone-id                  | String | 时区，必须遵循 `java.time.ZoneId` 的所含值。 例如：Asia/Shanghai                                          |       |
| logic-name-prefix        | String | 分片数据源或真实表的前缀格式                                                                             |       |
| datetime-lower           | String | 时间分片下界值，格式与 `yyyy-MM-dd HH:mm:ss` 的时间戳格式一致                                                 |       |
| datetime-upper           | String | 时间分片上界值，格式与 `yyyy-MM-dd HH:mm:ss` 的时间戳格式一致                                                 |       |
| sharding-suffix-pattern  | String | 分片数据源或真实表的后缀格式，必须遵循 Java DateTimeFormatter 的格式，必须和 `datetime-interval-unit` 保持一致。例如：yyyyMM |       |
| datetime-interval-unit   | String | 分片键时间间隔单位，必须遵循 Java ChronoUnit 的枚举值。例如：MONTHS                                              |       |
| datetime-interval-amount | int    | 分片键时间间隔，超过该时间间隔将进入下一分片                                                                     |       |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* 基于 CosId 的取模分片算法

基于 `me.ahoo.cosid:cosid-core` 的工具类实现的取模分片算法。
参考 https://github.com/apache/shardingsphere/issues/14047 的讨论。

类型：COSID_MOD

可配置属性：

| *属性名称*            | *数据类型* | *说明*           |
|-------------------|--------|----------------|
| mod               | int    | 分片数量           |
| logic-name-prefix | String | 分片数据源或真实表的前缀格式 |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-features-sharding-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## 基础设施插件

### 基础算法插件

#### 分布式主键生成插件

* NanoID

类型：NANOID

可配置属性： 无

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-nanoid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* CosId

类型: COSID

可配置属性：

| *属性名称*    | *数据类型* | *说明*                                                                                         | *默认值*       |
|-----------|--------|----------------------------------------------------------------------------------------------|-------------|
| id-name   | String | ID 生成器名称                                                                                     | `__share__` |
| as-string | bool   | 是否生成字符串类型ID: 将 `long` 类型 ID 转换成 62 进制 `String` 类型（`Long.MAX_VALUE` 最大字符串长度11位），并保证字符串 ID 有序性 | `false`     |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* CosId-Snowflake

类型: COSID_SNOWFLAKE

可配置属性：

| *属性名称*    | *数据类型* | *说明*                                                                                         | *默认值*           |
|-----------|--------|----------------------------------------------------------------------------------------------|-----------------|
| epoch     | String | 雪花 ID 算法的 EPOCH                                                                              | `1477929600000` |
| as-string | bool   | 是否生成字符串类型ID: 将 `long` 类型 ID 转换成 62 进制 `String` 类型（`Long.MAX_VALUE` 最大字符串长度11位），并保证字符串 ID 有序性 | `false`         |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-algorithm-key-generator-cosid</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

### 连接池插件

ShardingSphere-Proxy 支持常见的数据库连接池: HikariCP、C3P0、DBCP。

可以通过参数 `dataSourceClassName` 指定连接池，当不指定时，默认的的数据库连接池为 HikariCP。

* C3P0 连接池

配置示例：

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.mchange.v2.c3p0.ComboPooledDataSource
    url: jdbc:mysql://localhost:3306/ds_2
    username: root
    password:
```

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-data-source-pool-c3p0</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* DBCP 连接池

配置示例：

```yaml
dataSources:
  ds_0:
    dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource
    url: jdbc:mysql://localhost:3306/ds_3
    username: root
    password:
```

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-infra-data-source-pool-dbcp</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## 内核插件
    
### SQL 翻译器插件

* JooQ SQL 翻译器

类型：JOOQ

可配置属性：

无

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-kernel-sql-translator-jooq</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## JDBC 接入端插件

### JDBC Driver 配置插件

ShardingSphere-JDBC 提供了 JDBC 驱动，可以仅通过配置变更即可使用，无需改写代码。

* Apollo Driver 配置

加载 apollo 指定 namespace 中的 yaml 配置文件的 JDBC URL：
```
jdbc:shardingsphere:apollo:TEST.test_namespace
```

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-jdbc-driver-apollo</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

## 治理中心插件

### 治理中心集群模式持久化插件

* Nacos 持久化

类型：Nacos

适用模式：Cluster

可配置属性：

| *名称*                      | *数据类型* | *说明*              | *默认值*  |
|---------------------------|--------|-------------------|--------|
| clusterIp                 | String | 集群中的唯一标识          | 真实主机IP |
| retryIntervalMilliseconds | long   | 重试间隔毫秒数           | 500    |
| maxRetries                | int    | 客户端检查数据可用性的最大重试次数 | 3      |
| timeToLiveSeconds         | int    | 临时实例失效的秒数         | 30     |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-mode-cluster-repository-nacos</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```

* Consul 持久化

受 `com.ecwid.consul:consul-api:1.4.5` 的 Maven 模块的限制，使用者无法通过 gRPC 端口来连接到  Consul Agent。

`Consul` 实现的 `serverLists` 属性受设计使然，仅可通过 HTTP 端点连接到单个 Consul Agent。
`serverLists` 使用了宽松的 URL 匹配原则。
1. 当 `serverLists` 为空时，将解析到 `http://127.0.0.1:8500` 的 Consul Agent 实例。
2. 当 `serverLists` 为 `hostName` 时，将解析到 `http://hostName:8500` 的 Consul Agent 实例。
3. 当 `serverLists` 为 `hostName:port` 时，将解析到 `http://hostName:port` 的 Consul Agent 实例。
4. 当 `serverLists` 为 `http://hostName:port` 时，将解析到 `http://hostName:port` 的 Consul Agent 实例。
5. 当 `serverLists` 为 `https://hostName:port` 时，将解析到 `https://hostName:port` 的 Consul Agent 实例。

类型：Consul

适用模式：Cluster

可配置属性：

| *名称*                    | *数据类型* | *说明*      | *默认值* |
|-------------------------|--------|-----------|-------|
| timeToLiveSeconds       | String | 临时实例失效的秒数 | 30s   |
| blockQueryTimeToSeconds | long   | 查询请求超时秒数  | 60    |

Maven 依赖:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-plugin-mode-cluster-repository-consul</artifactId>
    <version>${RELEASE.VERSION}</version>
</dependency>
```
