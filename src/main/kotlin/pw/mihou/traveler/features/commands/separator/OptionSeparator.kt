package pw.mihou.traveler.features.commands.separator

object OptionSeparator {
    fun stream(text: String, accumulator: (text: String) -> Boolean) {

        val current = StringBuilder()

        var previous = ' '
        var inQuotations = false

        for ((index, char) in text.withIndex()) {
            fun append(): Boolean {
                val option = current.toString()
                val next = accumulator(option)

                current.clear()
                previous = ' '
                inQuotations = false

                return next
            }

            if (char == '"' || char == '\'') {
                // if not escaped
                if (previous != '\\') {
                    inQuotations = !inQuotations
                    if (index == (text.length - 1)) {
                        val next = append()
                        if (!next) {
                            break
                        }
                    }
                    continue
                }
            }

            if (char == ' ') {
                if (!inQuotations) {
                    val next = append()
                    if (!next) {
                        break
                    }
                    continue
                }
            }

            current.append(char)
            previous = char

            if (index == (text.length - 1)) {
                val next = append()
                if (!next) {
                    break
                }
                continue
            }
        }
    }
}