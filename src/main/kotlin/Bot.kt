import http.HttpApi

class Bot(config: Config) {
    companion object {
        lateinit var config: Config
        lateinit var httpApi: HttpApi
        lateinit var khlMessageHandler: KhlMessageHandler
    }
    init {
        Companion.config = config
        httpApi = HttpApi()
        khlMessageHandler = KhlMessageHandler()
        WebHook().initialize()
    }
}