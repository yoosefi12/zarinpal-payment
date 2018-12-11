package controllers

import javax.inject._
import play.api.Configuration
import play.api.mvc._
import com.zarinpal.PaymentGatewayImplementationService
import model.{Invoice, Repository}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject()(cc: ControllerComponents, conf: Configuration, paymentGatewayService: PaymentGatewayImplementationService, repository: Repository)(implicit exec: ExecutionContext) extends AbstractController(cc) {
  val merchantId = conf.getOptional[String]("zarinpal.merchantId")
  val startPayUrl = conf.get[String]("zarinpal.startpay.url")

  def index = Action {
    Ok(views.html.index())
  }

  def payment = Action.async { request =>

    val invoice = Invoice(amount = 500, description = "Something about your invoice", email = "yoosefi12@gmail.com", mobile = "+989359936376")
    val invoiceId = repository.addInvoice(invoice)

    def callPaymentRequest(merchantId: String) = paymentRequest(merchantId, invoiceId, invoice, request.host) map {
      case (100, authority) => Redirect(s"$startPayUrl/$authority")
      case _ => InternalServerError("Error in creating payment request.")
    } recover {
      case _ => InternalServerError("Can't create payment request.")
    }

    merchantId.fold(Future.successful(InternalServerError("MerchantId has not been provided.")))(callPaymentRequest)
  }

  def callback(invoiceId: String) = Action.async { request =>

    val paymentStatus = request.queryString("Status").lastOption
    val authority = request.queryString("Authority").lastOption

    def callPaymentVerification(merchantId: String, invoiceId: String, invoice: Invoice, authority: String): Future[Result] = {
      paymentVerification(merchantId, invoiceId, invoice, authority).map {
        case (100, ref) => Ok(s"Successful payment, ref : $ref")
        case (101, ref) => Ok(s"Successful payment (has been verified before), ref : $ref")
        case _ => Ok(s"Unsuccessful payment invoiceId : $invoiceId")
      } recover {
        case _ => InternalServerError("Can't verify payment request.")
      }
    }

    repository.getInvoice(invoiceId).fold {
      Future.successful(NotFound(s"Invoice no: $invoiceId not found."))
    } { invoice =>
      paymentStatus match {
        case Some("OK") if authority.nonEmpty => callPaymentVerification(merchantId.get, invoiceId, invoice, authority.get)
        case Some("NOK") => Future.successful(Ok(s"Unsuccessful payment invoiceId : $invoiceId"))
        case _ => Future.successful(Ok(s"Unsuccessful payment invoiceId : $invoiceId"))
      }
    }

  }

  def paymentRequest(merchantId: String, invoiceId: String, invoice: Invoice, host: String): Future[(Int, String)] = {
    val paymentStatus: javax.xml.ws.Holder[java.lang.Integer] = new javax.xml.ws.Holder[java.lang.Integer]()
    val authority: javax.xml.ws.Holder[java.lang.String] = new javax.xml.ws.Holder[java.lang.String]()
    val callback = s"http://$host${routes.HomeController.callback(invoiceId).url}"
    paymentGatewayService.paymentGatewayImplementationServicePort.paymentRequest(merchantId, invoice.amount, invoice.description, invoice.email, invoice.mobile, callback, paymentStatus, authority) map { _ =>
      (paymentStatus.value, authority.value)
    }
  }

  def paymentVerification(merchantId: String, invoiceId: String, invoice: Invoice, authority: String): Future[(Int, Long)] = {
    val verificationStatus: javax.xml.ws.Holder[java.lang.Integer] = new javax.xml.ws.Holder[java.lang.Integer]()
    val ref: javax.xml.ws.Holder[java.lang.Long] = new javax.xml.ws.Holder[java.lang.Long]()
    paymentGatewayService.paymentGatewayImplementationServicePort.paymentVerification(merchantId, authority, invoice.amount, verificationStatus, ref).map { x =>
      (verificationStatus.value, ref.value)
    }
  }


}
