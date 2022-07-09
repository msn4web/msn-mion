package pl.jill.webmsn.MSN

import pl.jill.webmsn.MSN.Events.SerializableEvent

import scala.collection.mutable.ArrayBuffer
import java.security.{MessageDigest, SecureRandom}
import java.math.BigInteger

class BoundMessengerClient(eml: String, messenger: EscargotMessenger, password: String) {
    protected var dead0: Boolean = true
    protected var created0: Long = System.currentTimeMillis
    protected var updated0: Long = System.currentTimeMillis
    protected val email0: String = eml
    protected val token0: String = new BigInteger(16 * 5, SecureRandom.getInstanceStrong).toString(64)
    protected val pwHash0: Array[Byte] = MessageDigest.getInstance("SHA3-384")
      .digest(password.getBytes("UTF-8"))
    
    protected var eventQueue: ArrayBuffer[SerializableEvent] = new ArrayBuffer[SerializableEvent]()
    protected val client0: EscargotMessenger = messenger
    
    def email: String = email0
    def token: String = token0
    def client: EscargotMessenger = client0
    def created: Long = created0
    def updated: Long = updated0
    def eventCount: Int = eventQueue.length
    def dead: Boolean = dead0
    
    messenger.addListener(new BoundEventListener(this))
    messenger.login()
    
    def checkPassword(pass: String): Boolean = {
        return MessageDigest.getInstance("SHA3-384")
          .digest(pass.getBytes("UTF-8"))
          .sameElements(pwHash0)
    }
    
    def touch(): Unit = {
        updated0 = System.currentTimeMillis
    }
    
    def events: ArrayBuffer[SerializableEvent] = this.synchronized {
        val events: ArrayBuffer[SerializableEvent] = eventQueue.clone()
        eventQueue = new ArrayBuffer[SerializableEvent]()
        
        return events
    }
    
    def enqueueEvent(event: SerializableEvent): Unit = this.synchronized {
        eventQueue += event
    }
    
    def markAsAlive(): Unit = {
        dead0 = false
    }
    
    def markAsDead(): Unit = {
        dead0 = true
    }
    
    def kill(): Unit = {
        markAsDead()
        client0.logout()
    }
}