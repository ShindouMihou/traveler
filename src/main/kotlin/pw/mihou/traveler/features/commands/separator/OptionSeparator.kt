package pw.mihou.traveler.features.commands.separator

object OptionSeparator {
    fun separate(text: String): List<String> {
        val separated = mutableListOf<String>()

        val current = StringBuilder()

        var previous = ' '
        var inQuotations = false

        for ((index, char) in text.withIndex()) {
            if (char == '"' || char == '\'') {
                // if not escaped
                if (previous != '\\') {
                    inQuotations = !inQuotations
                    continue
                }
            }

            fun append() {
                val option = current.toString()
                separated.add(option)

                current.clear()
                previous = ' '
                inQuotations = false
            }

            if (char == ' ') {
                if (!inQuotations) {
                    append()
                    continue
                }
            }

            if (index == (text.length - 1)) {
                append()
                continue
            }

            current.append(char)
            previous = char
        }

        return separated
    }
}