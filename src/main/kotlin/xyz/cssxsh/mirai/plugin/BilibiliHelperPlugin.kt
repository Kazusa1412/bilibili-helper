package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.warning
import org.openqa.selenium.chrome.ChromeDriverService
import xyz.cssxsh.bilibili.BilibiliClient
import xyz.cssxsh.mirai.plugin.command.*
import xyz.cssxsh.mirai.plugin.data.*
import java.io.File
import kotlin.time.*

object BilibiliHelperPlugin : KotlinPlugin(
    JvmPluginDescription("xyz.cssxsh.mirai.plugin.bilibili-helper", "0.1.0-dev-1") {
        name("bilibili-helper")
        author("cssxsh")
    }
) {

    internal lateinit var bilibiliClient : BilibiliClient
        private set

    private var service: ChromeDriverService? = null

    private fun serviceStart() {
        if (BilibiliChromeDriverConfig.driverPath.isNotBlank()) {
            runCatching {
                service = ChromeDriverService.Builder().apply {
                    usingDriverExecutable(File(BilibiliChromeDriverConfig.driverPath))
                    withVerbose(false)
                    withSilent(true)
                    withWhitelistedIps("")
                    usingPort(9515)
                }.build().apply { start() }
            }.onFailure {
                logger.warning({ "启动${BilibiliChromeDriverConfig.driverPath}失败" }, it)
            }
        }
    }

    private fun serviceStop() {
        service?.stop()
    }

    @ConsoleExperimentalApi
    override val autoSaveIntervalMillis: LongRange
        get() = (3).minutes.toLongMilliseconds()..(10).minutes.toLongMilliseconds()

    @ConsoleExperimentalApi
    override fun onEnable() {
        BilibiliTaskData.reload()
        BilibiliChromeDriverConfig.reload()
        BilibiliHelperSettings.reload()

        bilibiliClient = BilibiliClient(BilibiliHelperSettings.initCookies)
        serviceStart()

        BilibiliSubscribeCommand.start()
        BilibiliInfoCommand.start()

        BilibiliSubscribeCommand.register()
        BilibiliInfoCommand.register()
    }

    @ConsoleExperimentalApi
    override fun onDisable() {
        BilibiliSubscribeCommand.unregister()
        BilibiliInfoCommand.unregister()

        BilibiliSubscribeCommand.stop()
        BilibiliInfoCommand.stop()

        serviceStop()
    }
}