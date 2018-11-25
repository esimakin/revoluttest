import com.iyanuadelekan.kanary.handlers.AppHandler
import org.eclipse.jetty.server.Server
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppTest {
    companion object {
        val server = Server(10200)

        @BeforeClass
        @JvmStatic
        fun setUp() {
            server.handler = AppHandler(createApp())
            server.start()
        }

        @AfterClass
        fun tearDown() {
            server.stop()
        }
    }

    @Test
    fun test() {
        val sum = getSum()
        assertTrue { sum > BigDecimal.ONE }
        val before1 = getOneAmount("1")
        val before2 = getOneAmount("2")
        val transferResp = khttp.post("http://0.0.0.0:10200/transfer/money?from=1&to=2&amount=500")
        assertTrue { transferResp.statusCode == 200 }
        val after1 = getOneAmount("1")
        val after2 = getOneAmount("2")
        assertTrue { before1.subtract(BigDecimal(500)) == after1 }
        assertTrue { before2.add(BigDecimal(500)) == after2 }
        assertEquals(sum, getSum(), "The amount must be the same")
    }

    fun getSum(): BigDecimal {
        val users = khttp.get("http://0.0.0.0:10200/users/all").jsonArray
        assertFalse { users.length() <= 0 }
        return users
            .map { it as JSONObject }
            .map { it.get("amount").toString() }
            .map { BigDecimal(it) }
            .fold(BigDecimal.ZERO, BigDecimal::add)!!
    }

    private fun getOneAmount(userId: String): BigDecimal =
        BigDecimal(khttp.get("http://0.0.0.0:10200/users/one?userId=$userId").jsonObject.get("amount").toString())
}