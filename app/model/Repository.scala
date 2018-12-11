package model

import java.util.UUID

import com.google.inject.Singleton

@Singleton
class Repository {
  var dt: Map[String, Invoice] = Map()

  def addInvoice(invoice: Invoice): String = {
    val id = UUID.randomUUID().toString
    dt += (id -> invoice)
    id
  }

  def getInvoice(id: String) = {
    dt.contains(id) match {
      case true => Some(dt(id))
      case false => None
    }
  }

}
