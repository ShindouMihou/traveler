import org.junit.jupiter.api.Test
import pw.mihou.traveler.features.commands.schema.SchemaDecoder
import pw.mihou.traveler.features.commands.schema.SchemaDefinitionOption
import pw.mihou.traveler.features.commands.schema.SchemaOptionTypes
import kotlin.test.assertEquals

class SchemaDecoderTest {
    @Test
    fun `test schema decoder`() {
        val schema =  "[user:user] [string:message]"
        val definitions = SchemaDecoder.decode("test", schema)

        println(definitions)

        assert(definitions.options[0], "user", SchemaOptionTypes.User)
        assert(definitions.options[1], "message", SchemaOptionTypes.Text)
    }

    private fun assert(option: SchemaDefinitionOption, name: String, type: SchemaOptionTypes) {
        assertEquals(name, option.name)
        assertEquals(type, option.type)
    }
}