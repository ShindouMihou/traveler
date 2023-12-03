package pw.mihou.traveler.features.commands.separator

object OptionSeparator {
    fun separate(text: String): List<String> {
        val separated = mutableListOf<String>()

        val current = StringBuilder()

        var previous = ' '
        var inQuotations = false

        for ((index, char) in text.withIndex()) {
            fun append() {
                val option = current.toString()
                separated.add(option)

                current.clear()
                previous = ' '
                inQuotations = false
            }

            if (char == '"' || char == '\'') {
                // if not escaped
                if (previous != '\\') {
                    inQuotations = !inQuotations
                    if (index == (text.length - 1)) {
                        append()
                    }
                    continue
                }
            }

            if (char == ' ') {
                if (!inQuotations) {
                    append()
                    continue
                }
            }

            current.append(char)
            previous = char

            if (index == (text.length - 1)) {
                append()
                continue
            }
        }

        return separated
    }
}