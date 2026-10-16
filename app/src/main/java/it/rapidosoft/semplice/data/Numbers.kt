package it.rapidosoft.semplice.data

object Numbers {
    fun digits(number: String?): String = number?.filter { it.isDigit() } ?: ""

    /**
     * Confronto tollerante: "+39 333 1234567", "0039 3331234567" e "333 1234567"
     * sono lo stesso numero. Si confrontano le ultime 9 cifre (o meno, se il numero è corto).
     */
    fun same(a: String?, b: String?): Boolean {
        val x = digits(a)
        val y = digits(b)
        if (x.isEmpty() || y.isEmpty()) return false
        val shortest = minOf(x.length, y.length)
        if (shortest < 7) return x == y
        val k = minOf(9, shortest)
        return x.takeLast(k) == y.takeLast(k)
    }
}
