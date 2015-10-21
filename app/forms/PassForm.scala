package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign up process.
 */
object PassForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param password The full name of a user.
   */
  case class Data(password: String)
}
