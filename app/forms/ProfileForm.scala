package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the user profile.
 */
object ProfileForm {

  val mapp = mapping(
    "fullName" -> optional(nonEmptyText),
    "age" -> optional(number(min = 0,max = 100)),
    "sex" -> optional(nonEmptyText)
  )(Data.apply)(Data.unapply)

  /**
   * A play framework form.
   */
  val form = Form(mapp)


  /**
   * The form data.
   *
   * @param fullName The full name of a user.
   * @param sex The sex of the user.
   * @param age The age of the user.
   */
  case class Data(
    fullName:Option[String],
    age: Option[Int],
    sex: Option[String]
  )
}
