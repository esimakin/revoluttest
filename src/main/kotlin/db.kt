import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import java.math.BigDecimal

data class UserAccount(
    val id: Int,
    val name: String,
    val amount: BigDecimal
)

object Database {
    private val session = sessionOf("jdbc:sqlite:rtest.db", "", "")

    private val toUserAccount: (Row) -> UserAccount = {
        UserAccount(
            it.int("id"),
            it.string("name"),
            it.bigDecimal("amount")
        )
    }

    fun init() {
        session.run(
            queryOf(
                """
            CREATE TABLE IF NOT EXISTS user_accounts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                amount DECIMAL NOT NULL
            );
        """.trimIndent()
            ).asExecute
        )

        val list = session.run(queryOf("select * from user_accounts").map(toUserAccount).asList)
        if (list.isEmpty()) {
            session.run(queryOf("INSERT INTO user_accounts VALUES (1, 'John', 8548)").asExecute)
            session.run(queryOf("INSERT INTO user_accounts VALUES (2, 'Mr. X', 4001)").asExecute)
            session.run(queryOf("INSERT INTO user_accounts VALUES (3, 'Chi', 5800)").asExecute)
        }
    }

    fun getUserAccounts(): List<UserAccount> =
        session.run(queryOf("select * from user_accounts").map(toUserAccount).asList)

    fun getOneUserAcc(userId: Int): UserAccount? =
        session.run(queryOf("select * from user_accounts where id = ?", userId).map(toUserAccount).asSingle)

    fun updateUserAcc(userId: Int, newAmount: BigDecimal): Int =
        session.run(queryOf("update user_accounts set amount = ? where id = ?", newAmount, userId).asUpdate)

}