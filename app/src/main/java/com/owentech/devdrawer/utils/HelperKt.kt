package com.owentech.devdrawer.utils

inline fun consume(block: () -> Unit): Boolean {
    block()
    return true
}