import com.funnywolf.hollowkit.provider.*
import org.junit.Test

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/1/20
 */
class ProviderTest {

    @Test
    fun test() = Provider().run {
        put(String::class.java, LiveValue())
        put(Int::class.java, LiveValue())
        put(Long::class.java, LiveValue())

        assert(isSupport(String::class.java))
        assert(isSupport(Int::class.java))
        assert(isSupport(Long::class.java))
        assert(!isSupport(List::class.java))
        put(List::class.java, LiveValue())

        provide(String::class.java, "String")
        provide(Int::class.java, 12)
        provide(Long::class.java, 123L)
        provide(List::class.java, listOf("s1", "s2"))

        assert(valueOf(String::class.java) == "String")
        assert(valueOf(Int::class.java) == 12)
        assert(valueOf(Long::class.java) == 123L)
        assert(valueOf(List::class.java) == listOf("s1", "s2"))
        assert(valueOf(List::class.java)?.get(0) == "s1")

        var s: String? = ""
        provide(String::class.java, "New String")
        var listener: ValueListener<String>? = null
        listener = BasicListener {
            s = it
            listener?.dispose()
        }
        assert(valueOf(String::class.java, listener))
        provide(String::class.java, "String")
        assert(s == "New String")
    }

}