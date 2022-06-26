# khl-kt
一个开黑啦 Kotlin SDK  
## 快速开始
### 引入依赖
#### Maven
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.hank9999</groupId>
        <artifactId>khl-kt</artifactId>
        <version>0.0.1-fix1</version>
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
    implementation 'com.github.hank9999:khl-kt:0.0.1-fix1'
}
```
### 使用
```kotlin
fun main(args: Array<String>) {
    val bot = Bot(com.github.hank9999.khlKt.Config(token = "xxxx"))
    // 注册监听事件
    bot.registerClass(M())
    bot.registerMessageFunc { msg, cs -> message2(msg, cs) }
    bot.registerMessageFunc { msg, cs -> message3(msg, cs) }
}

class M {
    @Bot.OnMessage
    suspend fun message(msg: KhlMessage) {
        logger.info(msg.toString())
    }

    @Bot.OnEvent
    suspend fun event(event: KhlEvent) {
        logger.info(event.toString())
    }

    @Bot.OnFilter(FilterTypes.START_WITH, "/1234")
    suspend fun filter(msg: KhlMessage) {
        logger.info(msg.toString())
    }

    @Bot.OnCommand("test")
    suspend fun command(msg: KhlMessage) {
        logger.info(msg.toString())
    }
}
fun message2(msg: KhlMessage, cs: CoroutineScope) {
    println(msg)
}

fun message3(msg: KhlMessage, cs: CoroutineScope) {
    cs.launch {
        delay(500)
        logger.info(msg.toString())
    }
}
```
## LICENSE
本仓库采用 MIT 开源协议  
`CallAwait.kt` 和 `CallStackRecorder.kt` 来自于 [gildor/kotlin-coroutines-okhttp](https://github.com/gildor/kotlin-coroutines-okhttp) 使用 Apache License 2.0 授权