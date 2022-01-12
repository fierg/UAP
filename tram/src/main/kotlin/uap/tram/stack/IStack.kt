package uap.tram.stack

interface IStack<T> {

    operator fun set(index: Int, value: T)
    operator fun get(index: Int) : T
    fun pop() : T
}