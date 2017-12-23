package client

import play.api.Logger
import play.api.libs.json.Json

object TestHttpClient {

  val logger = Logger(this.getClass.getCanonicalName)

  def main(a: Array[String]): Unit = {


    case class PersonName(firstName: String, middleName: String, lastName: String)
    case class PhoneNumber(areaCode: Int, prefix: Int, lineNumber: Int) {
      assert(areaCode.toString.length == 3)
      assert(prefix.toString.length == 3)
      assert(lineNumber.toString.length == 4)
    }
    case class TelephoneNumbers(residence: PhoneNumber, mobile: PhoneNumber)

    case class ContactInformation(personName: PersonName, telephoneNumbers: TelephoneNumbers)

    val davidContactInformation = ContactInformation(PersonName("David", "Michael", "Hildebrandt"), TelephoneNumbers(PhoneNumber(416, 465, 9715), PhoneNumber(647, 300, 9715)))
    val suziContactInformation = ContactInformation(PersonName("Suzi", "Maristela", "Kuntze"), TelephoneNumbers(PhoneNumber(416, 465, 9715), PhoneNumber(647, 228, 5088)))

    val objectMap = ("david" -> davidContactInformation, "suzi" -> suziContactInformation)

//    println(Json.toJson(objectMap))

  }


}
