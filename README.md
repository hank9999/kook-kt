# kook-kt
[![](https://jitpack.io/v/hank9999/kook-kt.svg)](https://jitpack.io/#hank9999/kook-kt)  
一个 KOOK Kotlin SDK  
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
        <artifactId>kook-kt</artifactId>
        <version>0.0.2</version>
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
    implementation 'com.github.hank9999:kook-kt:0.0.2'
}
```
### 使用
```kotlin
fun main(args: Array<String>) {
    val bot = Bot(Config(token = "xxxx"))
    // 注册监听事件
    bot.registerClass(M())
    bot.registerMessageFunc { msg, cs -> message2(msg, cs) }
    bot.registerMessageFunc { msg, cs -> message3(msg, cs) }
    GlobalScope.launch {
        // 调用未封装 API 返回 JSON 对象, 需要在协程里调用
        // 获取第一个已加入服务器
        val data1 = HttpApi.request(Api.Guild.List())
        print(data.jsonArray[0])
        // 获取每一页第一个已加入服务器
        HttpApi.requestAsFlow(Api.Guild.List()).collect {
            print(it.jsonArray[0])
        }
        HttpApi.requestAsIterator(Api.Guild.List()).forEach {
            println(it.jsonArray[0])
        } 
        // 调用已封装 API, 需要在协程里调用
        // 获取网关
        println(HttpApi.Gateway.index()) // 直接是 URL
    }
}

class M {
    @Bot.OnMessage
    suspend fun message(msg: Message) {
        logger.info(msg.toString())
    }

    @Bot.OnEvent
    suspend fun event(event: Event) {
        logger.info(event.toString())
    }

    @Bot.OnFilter(FilterTypes.START_WITH, "/1234")
    suspend fun filter(msg: Message) {
        logger.info(msg.toString())
        msg.reply("1234") // 快捷回复
    }

    @Bot.OnCommand("test")
    suspend fun command(msg: Message) {
        logger.info(msg.toString())
        msg.send("1234") // 快速发送
    }
}
fun message2(msg: Message, cs: CoroutineScope) {
    println(msg)
}

fun message3(msg: Message, cs: CoroutineScope) {
    cs.launch {
        delay(500)
        logger.info(msg.toString())
    }
}
```
## LICENSE
本仓库采用 MIT 开源协议  
`CallAwait.kt` 和 `CallStackRecorder.kt` 来自于 [gildor/kotlin-coroutines-okhttp](https://github.com/gildor/kotlin-coroutines-okhttp) 使用 Apache License 2.0 授权