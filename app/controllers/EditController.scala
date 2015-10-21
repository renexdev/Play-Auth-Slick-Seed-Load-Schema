package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms._
import models.{User,Profile}
import models.services.UserService
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * The Pass controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param avatarService The avatar service implementation.
 * @param passwordHasher The password hasher implementation.
 */
class EditController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  avatarService: AvatarService,
  passwordHasher: PasswordHasher)
  extends Silhouette[User, CookieAuthenticator] {

  /**
   * change the password.
   *
   * @return The result to display.
   */
  def pass = SecuredAction.async { implicit request =>
    val c_user = request.identity
    val Some(email:String) = c_user.email
    PassForm.form.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest(views.html.pass(formWithError)(c_user))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val authInfo = passwordHasher.hash(data.password)
            for {
              authInfo <- authInfoRepository.update(loginInfo, authInfo)
              authenticator <- env.authenticatorService.create(loginInfo)
              value <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.admin()))
            } yield result
          case None =>
            Future.successful(Redirect(routes.ApplicationController.pass()).flashing("error" -> Messages("user.non-existent")))
        }
      }
    )
  }

  /**
   * change The profile.
   *
   * @return The result to display.
   */
  def profile = SecuredAction.async { implicit request =>
    val c_user = request.identity
    val Some(email:String) = c_user.email
    ProfileForm.form.bindFromRequest.fold(
      formWithError => Future.successful(BadRequest(views.html.profile(formWithError)(c_user))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val user = User(
              userID = c_user.userID,
              loginInfo = loginInfo,
              email = c_user.email,
              profile = Profile(data.fullName,data.age,data.sex),
              avatarURL = None
            )
            for {
              user <- userService.update(user.copy(avatarURL = c_user.avatarURL))
              authenticator <- env.authenticatorService.create(loginInfo)
              value <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.admin()))
            } yield result
          case None =>
            Future.successful(Redirect(routes.ApplicationController.profile()).flashing("error" -> Messages("user.non-existent")))
        }
      }
    )

  }


}
