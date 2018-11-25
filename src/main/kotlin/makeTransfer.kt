import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object Locks {
    private val map: MutableMap<Int, Lock> = ConcurrentHashMap()

    fun getLock(id: Int): Lock {
        val lock = map.getOrDefault(id, ReentrantLock())
        map[id] = lock
        return lock
    }
}

fun makeTransfer(fromId: Int, toId: Int, amount: BigDecimal): Result {
    val toTransfer = amount.setScale(2, RoundingMode.HALF_UP)
    if (toTransfer <= BigDecimal.ZERO) return Result(false, "Incorrect amount to transfer")

    val db = Database

    val from = db.getOneUserAcc(fromId) ?: return Result(false, "The 'from' user is not found")
    val to = db.getOneUserAcc(toId) ?: return Result(false, "The 'to' user is not found")

    val seq = if (fromId < toId) Pair(fromId, toId) else Pair(toId, fromId)

    val firstLock = Locks.getLock(seq.first)
    firstLock.lock()
    try {
        if (from.amount < toTransfer) return Result(false, "Insufficient money amount on 'from' user")
        val secondLock = Locks.getLock(seq.second)
        secondLock.lock()
        try {
            val newFrom = from.amount.subtract(toTransfer)
            var r = db.updateUserAcc(from.id, newFrom)
            if (r <= 0) return Result(false, "Failed to update 'from' user")

            val newTo = to.amount.add(toTransfer)
            r = db.updateUserAcc(to.id, newTo)
            if (r <= 0) return Result(false, "Failed to update 'to' user")
        } finally {
            secondLock.unlock()
        }
    } finally {
        firstLock.unlock()
    }

    return Result(true, "");
}

data class Result(val isSuccess: Boolean, val errorMsg: String)
