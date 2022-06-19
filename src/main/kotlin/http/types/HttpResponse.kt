package http.types

data class HttpResponse(
    val code: Int = 200,
    val body: String = "",
    val headers: Map<String, List<String>> = emptyMap()
)
