package pw.mihou.traveler.configuration

class TravelerConfiguration internal constructor() {
    val prefix = TravelerPrefixConfiguration()
}

typealias PrefixLoader = suspend (server: Long?) -> String
class TravelerPrefixConfiguration internal constructor() {
    var loader: PrefixLoader = { "%" }
}
