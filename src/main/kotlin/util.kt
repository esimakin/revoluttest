fun parseQueryString(str: String?): Map<String, List<String>> {
    if (str == null) emptyMap<String, List<String>>()
    return str!!
        .split("&")
        .map {
            val i = it.indexOf("=")
            val key = if (i > 0) it.substring(0, i) else it
            val value = if (i > 0 && it.length > i + 1) it.substring(i + 1) else null
            Pair(key, value)
        }
        .groupBy {
            it.first
        }
        .mapValues { entry ->
            entry.value.mapNotNull { it.second }
        }
        .filter { it.value.isNotEmpty() }
}