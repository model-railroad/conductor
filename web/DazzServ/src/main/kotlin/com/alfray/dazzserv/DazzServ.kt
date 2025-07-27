package com.alfray.dazzserv

class DazzServ {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ds = DazzServ()
            ds.onStart()
        }
    }

    init {
        println("Hello World!")
    }

    fun onStart() {
        println("DazzServ.onStart")
    }
}
