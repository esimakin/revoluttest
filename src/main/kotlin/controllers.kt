import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.iyanuadelekan.kanary.core.KanaryController
import com.iyanuadelekan.kanary.helpers.http.request.done
import com.iyanuadelekan.kanary.helpers.http.response.send
import com.iyanuadelekan.kanary.helpers.http.response.sendJson
import com.iyanuadelekan.kanary.helpers.http.response.withStatus
import org.eclipse.jetty.server.Request
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import java.math.BigDecimal
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UserController : KanaryController() {

    private val db = Database
    private val mapper = ObjectMapper()

    val userAccToJson: (UserAccount) -> ObjectNode = {
        val oneUserJson = mapper.createObjectNode()
        oneUserJson.put("id", it.id)
        oneUserJson.put("name", it.name)
        oneUserJson.put("amount", it.amount)
        oneUserJson
    }

    fun getAll(base: Request, req: HttpServletRequest, resp: HttpServletResponse) {
        val usersJson = mapper.createArrayNode()
        Database.getUserAccounts().forEach {
            usersJson.add(userAccToJson(it))
        }
        resp withStatus 200 sendJson usersJson
        base.done()
    }

    fun getOne(base: Request, req: HttpServletRequest, resp: HttpServletResponse) {
        val q = parseQueryString(base.queryString)
        if (!q.containsKey("userId")) {
            resp withStatus 400 send "userId is not specified"
            base.done()
            return
        }


        val userId = q.get("userId")!![0]
        val user = Database.getOneUserAcc(userId.toInt())
        if (user == null) {
            resp withStatus 404
        } else {
            resp withStatus 200 sendJson userAccToJson(user)
        }
        base.done()
    }
}

class MoneyTransferController : KanaryController() {

    fun transfer(base: Request, req: HttpServletRequest, resp: HttpServletResponse) {
        val q = parseQueryString(base.queryString)
        if (!q.containsKey("from") || !q.containsKey("to") || !q.containsKey("amount")) {
            resp withStatus 400 send "The 'from' user id, 'to' user id and 'amount' of money code should be specified"
            base.done()
            return
        }

        if (q["from"]!!.size > 1) {
            resp withStatus 400 send "There are more than one 'from' user id specified"
            base.done()
            return
        }
        if (q["to"]!!.size > 1) {
            resp withStatus 400 send "There are more than one 'to' user id specified"
            base.done()
            return
        }
        if (q["amount"]!!.size > 1) {
            resp withStatus 400 send "There are more than one 'amount' of money specified"
            base.done()
            return
        }

        val fromId: Int
        val toId: Int
        val amount: BigDecimal
        try {
            fromId = q["from"]!![0].toInt()
        } catch (e: NumberFormatException) {
            resp withStatus 400 send "Incorrect 'from' is specified"
            base.done()
            return
        }
        try {
            toId = q["to"]!![0].toInt()
        } catch (e: NumberFormatException) {
            resp withStatus 400 send "Incorrect 'to' is specified"
            base.done()
            return
        }
        try {
            amount = BigDecimal(q["amount"]!![0])
        } catch (e: NumberFormatException) {
            resp withStatus 400 send "Incorrect amount is specified"
            base.done()
            return
        }

        val result = makeTransfer(fromId, toId, amount)
        if (result.isSuccess) {
            resp withStatus 200 send "Successful transfer"
        } else {
            resp withStatus 200 send result.errorMsg
        }
        base.done()
    }
}

