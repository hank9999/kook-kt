package com.github.hank9999.kook.rest.service

import com.github.hank9999.kook.rest.request.RequestBuilder
import com.github.hank9999.kook.rest.request.RequestHandler
import com.github.hank9999.kook.rest.response.PagedData
import com.github.hank9999.kook.rest.route.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * REST 服务基类
 *
 * 提供 [call] 和 [callPaged] 两个核心方法,
 * 所有具体 Service (UserService, GuildService 等) 继承此类。
 */
@OptIn(ExperimentalContracts::class)
abstract class RestService(
    @PublishedApi internal val requestHandler: RequestHandler,
) {
    /**
     * 执行单次 API 调用
     *
     * ```kotlin
     * suspend fun getMe(): SelfUser = call(Route.UserMe)
     *
     * suspend fun viewUser(userId: String): IUser = call(Route.UserView) {
     *     parameter("user_id", userId)
     * }
     * ```
     */
    @PublishedApi
    internal suspend inline fun <T> call(
        route: Route<T>,
        builder: RequestBuilder<T>.() -> Unit = {},
    ): T {
        contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
        val requestBuilder = RequestBuilder(route).apply(builder)
        requestHandler.intercept(requestBuilder)
        val request = requestBuilder.build()
        return requestHandler.handle(request)
    }

    /**
     * 分页调用, 返回 [Flow] 逐页 emit items
     *
     * 自动迭代 page 直到 page >= pageTotal。
     * [builder] 的参数 page 是当前页码, 可用于添加额外查询条件。
     *
     * ```kotlin
     * fun listGuilds(): Flow<List<Guild>> = callPaged(Route.GuildList)
     *
     * fun listGuildUsers(guildId: String): Flow<List<GuildUser>> =
     *     callPaged(Route.GuildUserList) { _ ->
     *         parameter("guild_id", guildId)
     *     }
     * ```
     */
    @PublishedApi
    internal inline fun <T> callPaged(
        route: Route<PagedData<T>>,
        pageSize: Int = 50,
        crossinline builder: RequestBuilder<PagedData<T>>.(page: Int) -> Unit = {},
    ): Flow<List<T>> = flow {
        var currentPage = 1
        while (true) {
            val pagedData = call(route) {
                parameter("page", currentPage)
                parameter("page_size", pageSize)
                builder(currentPage)
            }
            emit(pagedData.items)

            if (currentPage >= pagedData.meta.pageTotal) break
            currentPage++
        }
    }
}
