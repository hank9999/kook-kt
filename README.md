# kook-kt
[![](https://jitpack.io/v/hank9999/kook-kt.svg)](https://jitpack.io/#hank9999/kook-kt)  
一个 KOOK Kotlin SDK  
## 快速开始
### 引入依赖
#### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.hank9999</groupId>
        <artifactId>kook-kt</artifactId>
        <version>0.0.4</version>
    </dependency>
</dependencies>
```
#### Gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    implementation 'com.github.hank9999:kook-kt:0.0.4'
}
```
### 使用
移步 [kook-kt-example](https://github.com/hank9999/kook-kt-example) 示例项目

## LICENSE
本仓库采用 MIT 开源协议  
`CallAwait.kt` 和 `CallStackRecorder.kt` 来自于 [gildor/kotlin-coroutines-okhttp](https://github.com/gildor/kotlin-coroutines-okhttp) 使用 Apache License 2.0 授权