package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

/**
 * The user object.
 *
 * @param userID The unique ID of the user.
 * @param loginInfo The linked login info.
 * @param email Maybe the email of the authenticated provider.
 * @param profile Maybe the profile of the authenticated user.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 */
case class User(
  userID: UUID,
  loginInfo: LoginInfo,
  email: Option[String],
  profile:Profile,
  avatarURL: Option[String]) extends Identity

/**
 * The user profile object.
 *
 * @param fullName Maybe the full name of the authenticated user.
 * @param age Maybe the age of the authenticated user.
 * @param sex Maybe the sex of the authenticated user.
 */
case class Profile(fullName:Option[String],age:Option[Int],sex:Option[String])
