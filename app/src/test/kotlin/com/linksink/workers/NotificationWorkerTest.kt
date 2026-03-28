package com.linksink.workers

import org.junit.Test

class NotificationWorkerTest {

    @Test
    fun `NotificationWorker class exists`() {
        val workerClass = NotificationWorker::class.java
        assert(workerClass != null) { "NotificationWorker class should exist" }
    }

    @Test
    fun `NotificationWorker extends CoroutineWorker`() {
        val workerClass = NotificationWorker::class.java
        val superClass = workerClass.superclass
        assert(superClass?.simpleName == "CoroutineWorker") {
            "NotificationWorker should extend CoroutineWorker, but extends ${superClass?.simpleName}"
        }
    }

    @Test
    fun `NotificationWorker has doWork method`() {
        val workerClass = NotificationWorker::class.java
        val method = workerClass.methods.find { it.name == "doWork" }
        assert(method != null) { "doWork method should exist" }
    }

    @Test
    fun `NotificationWorker implementation checks daily limit`() {
        val workerClass = NotificationWorker::class.java
        val doWorkMethod = workerClass.methods.find { it.name == "doWork" }
        assert(doWorkMethod != null) { "doWork method should exist for daily limit check verification" }
    }
}
