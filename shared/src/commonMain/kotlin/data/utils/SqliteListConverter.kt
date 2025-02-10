package data.utils

/**
 * Utility class for converting between SQLite GROUP_CONCAT strings and typed lists
 */
class SqliteListConverter {
    companion object {
            /**
             * Converts a GROUP_CONCAT string to a list of single values
             * @param str The concatenated string from SQLite
             * @param defaultSize The size of the default list if string is null
             * @param defaultValue The default value for each item
             */
            fun <T> fromString(
                str: String?,
                defaultSize: Int,
                defaultValue: T
            ): List<T> {
                try {
                    requireNotNull(str)
                    return str.split(",").map { it as T }
                } catch (e: Exception) {
                    return List(defaultSize) { defaultValue }
                }
            }
        }
    }