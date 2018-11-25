import com.iyanuadelekan.kanary.app.KanaryApp
import com.iyanuadelekan.kanary.core.KanaryRouter
import com.iyanuadelekan.kanary.handlers.AppHandler
import com.iyanuadelekan.kanary.server.Server
import javax.servlet.http.HttpServletRequest

val requestLogger: (HttpServletRequest?) -> Unit = { req ->
    req?.let { println("${req.method} request to '${req.pathInfo}'") }
}

fun main(args: Array<String>) {
    val server = Server()

    server.handler = AppHandler(createApp())
    server.listen(8001)
}

fun createApp(): KanaryApp {
    val app = KanaryApp()
    val router = KanaryRouter()

    val userController = UserController()
    val transferController = MoneyTransferController()

    router on "users/" use userController
    router.get("all/", userController::getAll)
    router.get("one/", userController::getOne)

    router on "transfer/" use transferController
    router.post("money/", transferController::transfer)

    app.mount(router)
    app.use(requestLogger)

    Database.init()
    return app
}

