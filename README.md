## 阿里巴巴遵循他们自己的p3c规范吗？

https://github.com/alibaba/p3c

阿里巴巴重磅发布了p3c规范，那么人们不禁要问，阿里巴巴开源项目遵循他们自己的规范吗？

你可以运行以下命令对[GitHub Alibaba](https://github.com/alibaba)组织下的所有Java非Android项目运行p3c规范检查：

```
groovy DoesAlibabaFollowTheirOwnGuidelines.groovy
``` 

只进行最高优先级的检查：

```
groovy -DminimumPriority=1 DoesAlibabaFollowTheirOwnGuidelines.groovy
``` 

注意在JDK8上运行，因为有些项目的构建不支持11；同时保证`PATH`中包含`mvn`和`git`。这会对所有的项目运行`mvn clean install -DskipTests`，期间会执行`mvn pmd:check`。

| 项目名 | 是否使用了pmd-p3c | default | -DminimumPriority=1|PR及状态|备注|
|---|---|---|---|---|---|
|fastjson|否|2926 PMD violations|91 PMD violations||
|druid|否|10066 PMD violations|216 PMD violations|||
|[https://github.com/alibaba/arthas](arthas)|是|? PMD violations|build successful|Merged: https://github.com/alibaba/arthas/pull/568|[维护者说](https://github.com/alibaba/arthas/pull/568#issuecomment-472721084):不过Arthas不打算引入`maven-pmd-plugin`插件，因为代码风格有时候是很主观的。|
|canal|否|42 PMD violations|3 PMD violations|||
|fescar|否|5 PMD violations|build successful|||
|Sentinel|否|143 PMD violations|3 PMD violations|||
|nacos|是|build successful|build successful|||
|otter|否|build failure|build failure||https://github.com/alibaba/otter/issues/290|
|easyexcel|否|44 PMD violations|build successful|||
|jstorm|否|19183 PMD violations|2593 PMD violations|||
|cobar-driver|否|35 PMD violations|1 PMD violations|||
|cobar-server|否|2327 PMD violations|356 PMD violations|||
|cobar-manager|否|346 PMD violations|47 PMD violations|||
|jvm-sandbox|否|20 PMD violations|11 PMD violations|||
|DataX|否|179 PMD violations|6 PMD violations|||
|TProfiler|否|70 PMD violations|2 PMD violations|||
|dubbo-spring-boot-starter|否|9 PMD violations|build successful|||
|jetcache|否|5 PMD violations|2 PMD violations|||
|mdrill-trunk|否|4442 PMD violations|713 PMD violations|||
|yugong|否|297 PMD violations|82 PMD violations|||
|QLExpress|否|637 PMD violations|169 PMD violations|||
|taokeeper|否|build failure|build failure||https://github.com/alibaba/taokeeper/issues/31|
|simpleimage|否|build failure|unknown||https://github.com/alibaba/simpleimage/issues/16 |
|asyncload|否|112 PMD violations|1 PMD violations|||
|p3c-eclipse-plugin|否|build successful|build successful|||
|p3c-p3c-pmd|否|2 PMD violations|build successful|Open: https://github.com/alibaba/p3c/pull/466||

注意其中的数字并不一定是整个项目violation数量，它可能只代表一个子项目中的violation数量。

### 帮助阿里巴巴遵守阿里巴巴规范

如果你想要帮助阿里巴巴遵守阿里巴巴规范，你可以如此做：

- 在上面的列表中挑选一个你喜欢的、未使用p3c-pmd的项目。注意选择活跃项目，否则可能处于无人维护状态，这可以通过项目提交状态甄别。
- 在`pom.xml`中加入：
```
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.8</version>
    <configuration>
      <sourceEncoding>${project.build.sourceEncoding}</sourceEncoding>
      <targetJdk>1.8</targetJdk>
      <printFailingErrors>true</printFailingErrors>
      <rulesets>
        <ruleset>rulesets/java/ali-comment.xml</ruleset>
        <ruleset>rulesets/java/ali-concurrent.xml</ruleset>
        <ruleset>rulesets/java/ali-constant.xml</ruleset>
        <ruleset>rulesets/java/ali-exception.xml</ruleset>
        <ruleset>rulesets/java/ali-flowcontrol.xml</ruleset>
        <ruleset>rulesets/java/ali-naming.xml</ruleset>
        <ruleset>rulesets/java/ali-oop.xml</ruleset>
        <ruleset>rulesets/java/ali-orm.xml</ruleset>
        <ruleset>rulesets/java/ali-other.xml</ruleset>
        <ruleset>rulesets/java/ali-set.xml</ruleset>
      </rulesets>
    </configuration>
    <executions>
      <execution>
        <phase>verify</phase>
        <goals>
          <goal>check</goal>
        </goals>
      </execution>
    </executions>
    <dependencies>
      <dependency>
        <groupId>com.alibaba.p3c</groupId>
        <artifactId>p3c-pmd</artifactId>
        <version>1.3.6</version>
      </dependency>
    </dependencies>
  </plugin>
```

- 运行`mvn clean install -DskipTests -DminimumPriority=1`，修正列出的violations。如果你拿不准如何修改，可以使用`@SuppressWarnings("PMD.<RuleName>")`（这是编译期注解，放心使用），这样至少可以保证未来的代码不会变得更差。
- 提交PR。
- 加分项：你可以尝试不加`-DminimumPriority=1`，修正所有violations。代价是工作量可能增长数倍。

-------------------------

下面是广告时间！


<a href="https://detail.tmall.com/item.htm?spm=a1zb6.8232479.0.0.17f533e9VNZxB2&id=575107529181&cm_id=140105335569ed55e27b&abbucket=15"><img src="https://img.alicdn.com/tfs/TB1fJAjcirpK1RjSZFhXXXSdXXa-7068-1201.jpg"></a>
2018年9月22日，在2018杭州云栖大会上，召开《码出高效：Java 开发手册》新书发布会，并宣布将图书所有收益均捐赠于技术公益项目“83行代码计划”。

阿里巴巴正式在2018杭州云栖大会《开发者生态峰会》上，由阿里巴巴高年级同学中间件负责人林昊、阿里巴巴研究员刘湘雯、阿里巴研究员刘国华，OpenJDK社区Committer杨晓峰，全栈视障工程师蔡勇斌，电子工业出版社博文视点出版公司总经理郭立以及两位图书作者杨冠宝（孤尽）和高海慧（鸣莎）重磅大咖联合发布新书[《码出高效：Java开发手册》（跳转至天猫书店）](https://detail.tmall.com/item.htm?spm=a1zb6.8232479.0.0.17f533e9VNZxB2&id=575107529181&cm_id=140105335569ed55e27b&abbucket=15)，并宣布将图书所有收益均捐赠于技术公益项目“83行代码计划”，第一个“83行代码计划”行动，将围绕着帮助盲人工程师，开发更多无障碍化产品，让盲人上网更便捷。未来，我们会坚持用技术为公益行业赋能，也希望更多人成为技术受益者<
  ![](https://img.alicdn.com/tfs/TB1fxuedAvoK1RjSZFDXXXY3pXa-1465-603.png)

 2017年10月14日杭州云栖大会，Java代码规约扫描插件全球首发仪式正式启动，规范正式以插件形式公开走向业界，引领Java语言的规范之路。目前，插件已在[云效公有云产品](https://www.aliyun.com/product/yunxiao)中集成，[立即体验](https://rdc-test.aliyun.com)！（云效>公有云>设置->测试服务->阿里巴巴Java代码规约）。

# P3C

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

## <font color="green">Preface</font>
> We are pleased to present Alibaba Java Coding Guidelines which consolidates the best programming practices over the years from Alibaba Group's technical teams. A vast number of Java programming teams impose demanding requirements on code quality across projects as we encourage reuse and better understanding of each other's programs. We have seen many programming problems in the past. For example, defective database table structures and index designs may cause software architecture flaws and performance risks. Another example is confusing code structures being difficult to maintain. Furthermore, vulnerable code without authentication is prone to hackers’ attacks. To address these kinds of problems, we developed this document for Java developers at Alibaba.
 
For more information please refer the *Alibaba Java Coding Guidelines*:
- 中文版: *[阿里巴巴Java开发手册](https://github.com/alibaba/p3c/blob/master/%E9%98%BF%E9%87%8C%E5%B7%B4%E5%B7%B4Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%EF%BC%88%E8%AF%A6%E5%B0%BD%E7%89%88%EF%BC%89.pdf)*
- English Version: *[Alibaba Java Coding Guidelines](https://alibaba.github.io/Alibaba-Java-Coding-Guidelines)*
- 《阿里巴巴Java开发手册》书籍版天猫官方店: *[阿里巴巴Java开发手册最新版](https://detail.tmall.com/item.htm?spm=a1z2e.8325951.feedDetail.4.3315431gklIXe&id=562626792765&ns=1&abbucket=14)*
- 《码出高效》书籍版天猫官方店: *[码出高效：Java开发手册](https://detail.tmall.com/item.htm?spm=a230r.1.14.40.7dee7d6bwpO82U&id=575107529181&ns=1&abbucket=20)*

## <font color="green">Introduction</font>
The project consists of 3 parts:  
- [PMD implementations](p3c-pmd)  
- [IntelliJ IDEA plugin](idea-plugin)  
- [Eclipse plugin](eclipse-plugin)   

## <font color="green">Rules</font>
<font color="blue">Forty-nine rules are realized based on PMD, please refer the P3C-PMD documentation for more detailed information. Four rules are implemented within IDE plugins (IDEA and Eclipse) as follows:</font>  

- ``[Mandatory]`` Using a deprecated class or method is prohibited.  
   Note: For example, decode(String source, String encode) should be used instead of the deprecated method decode(String encodeStr). Once an interface has been deprecated, the interface provider has the obligation to provide a new one. At the same time, client programmers have the obligation to check out what its new implementation is.
   
- ``[Mandatory]`` An overridden method from an interface or abstract class must be marked with @Override annotation.
   Counter example: For getObject() and get0bject(), the first one has a letter 'O', and the second one has a number '0'. To accurately determine whether the overriding is successful, an @Override annotation is necessary. Meanwhile, once the method signature in the abstract class is changed, the implementation class will report a compile-time error immediately.
   
- ``[Mandatory]`` A static field or method should be directly referred by its class name instead of its corresponding object name.

- ``[Mandatory]`` The usage of hashCode and equals should follow:
    1. Override hashCode if equals is overridden.
    2. These two methods must be overridden for Set since they are used to ensure that no duplicate object will be inserted in Set.
    3. These two methods must be overridden if self-defined object is used as the key of Map.
   Note: String can be used as the key of Map since these two methods have been rewritten.

## Join us
If you have any questions or comments, please contact junlie by email at caikang.ck@alibaba-inc.com, and please join us to make project P3C perfect for more programmers.

Please follow our WeChat official account as ali_yunxiao below:

![](https://gw.alicdn.com/tfscom/TB1TrNcXjv85uJjSZFNXXcJApXa.png)
