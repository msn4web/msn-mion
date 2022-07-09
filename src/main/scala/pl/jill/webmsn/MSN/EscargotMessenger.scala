package pl.jill.webmsn.MSN

import net.sf.jml.Email
import net.sf.jml.impl.SimpleMessenger

class EscargotMessenger(email: Email, password: String) extends SimpleMessenger(email, password) {
    override def login(): Unit = super.login("msnmsgr.escargot.chat", 1863)
}